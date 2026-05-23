package com.uuriturg.scraper.scraper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KvEeScraper implements RentalScraper {

    private static final String BASE_URL   = "https://www.kv.ee";
    private static final String SEARCH_URL =
            BASE_URL + "/search?deal_type=2&county=12&parish=1063&property_type=1";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 3;

    @Override
    public String getSourceName() {
        return "KV_EE";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            int start = page * PAGE_SIZE;
            String url = SEARCH_URL + "&start=" + start;
            log.info("KV.ee: fetching page start={}", start);

            try {
                String html = fetchWithWget(url);
                if (html == null || html.isBlank()) {
                    log.warn("KV.ee: empty response for start={}", start);
                    break;
                }

                Document doc = Jsoup.parse(html, BASE_URL);
                Elements articles = doc.select("article[class*=object-type-apartment]");
                log.info("KV.ee: start={} found {} articles", start, articles.size());

                if (articles.size() < 5) break;

                for (Element article : articles) {
                    Listing listing = parseArticle(article);
                    if (listing != null && seen.add(listing.getExternalId())) {
                        all.add(listing);
                    }
                }
            } catch (Exception e) {
                log.error("KV.ee: failed at start={}: {}", start, e.getMessage());
                break;
            }
        }

        log.info("KV.ee scrape complete — {} listings", all.size());
        return all;
    }

    // wget has a different TLS fingerprint than Java's HttpClient, so it bypasses Cloudflare
    private String fetchWithWget(String url) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "wget", "-q", "-O", "-",
                "--timeout=20",
                "--user-agent=" + USER_AGENT,
                "--header=Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "--header=Accept-Language: et-EE,et;q=0.9,en-US;q=0.8,en;q=0.7",
                "--header=Referer: https://www.kv.ee/",
                url
        );
        pb.redirectErrorStream(false);
        Process proc = pb.start();

        byte[] out;
        try (InputStream is = proc.getInputStream()) {
            out = is.readAllBytes();
        }
        boolean finished = proc.waitFor(25, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            log.warn("KV.ee: wget timed out for {}", url);
            return null;
        }
        int exitCode = proc.exitValue();
        if (exitCode != 0) {
            log.warn("KV.ee: wget exit {} for {}", exitCode, url);
            return null;
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    private Listing parseArticle(Element article) {
        String objectId = article.attr("data-object-id");
        if (objectId == null || objectId.isBlank()) return null;

        String relUrl = article.attr("data-object-url");
        String url = (relUrl == null || relUrl.isBlank()) ? SEARCH_URL : BASE_URL + relUrl;

        // Images are lazy-loaded — real URL is in data-src, not src
        String imageUrl = article.select("div.images img[data-src]").attr("data-src");

        Element titleAnchor = article.selectFirst("div.description h2 a[data-skeleton=object]");
        String fullTitle  = titleAnchor != null ? titleAnchor.text() : "";
        Element strong    = article.selectFirst("div.description h2 a strong");
        String street     = strong != null ? strong.text() : null;
        String neighborhood = extractNeighborhoodFromTitle(fullTitle);

        String roomsText = article.select("div.rooms").text().trim();
        Integer rooms    = parseIntSafe(roomsText);

        String areaText  = article.select("div.area").text()
                .replaceAll("[^0-9.,]", "").replace(",", ".");
        BigDecimal size  = parseBigDecimalSafe(areaText);

        Element priceEl  = article.selectFirst("div.price");
        String priceText = priceEl != null ? priceEl.ownText().replaceAll("[^0-9]", "") : "";
        BigDecimal price = parseBigDecimalSafe(priceText);

        return Listing.builder()
                .source(Source.KV_EE)
                .externalId("kv-" + objectId)
                .title(fullTitle.isBlank() ? "KV.ee listing" : fullTitle)
                .price(price)
                .size(size)
                .rooms(rooms)
                .neighborhood(neighborhood)
                .street(street)
                .city("Tartu")
                .url(url)
                .imageUrl(imageUrl.isBlank() ? null : imageUrl)
                .synthetic(false)
                .build();
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private BigDecimal parseBigDecimalSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private String extractNeighborhoodFromTitle(String title) {
        if (title == null) return null;
        String lower = title.toLowerCase(Locale.ROOT);
        for (String h : new String[]{"kesklinn","ülejõe","tammelinn","annelinn",
                "karlova","veeriku","tähtvere","supilinn","ränilinn","maarjamõisa"}) {
            if (lower.contains(h)) return capitalize(h);
        }
        String[] parts = title.split(",");
        if (parts.length >= 2) {
            String candidate = parts[1].trim();
            if (!candidate.toLowerCase(Locale.ROOT).contains("tartu") && candidate.length() < 30) {
                return candidate;
            }
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
