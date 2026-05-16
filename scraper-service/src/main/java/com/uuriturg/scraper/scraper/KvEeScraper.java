package com.uuriturg.scraper.scraper;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KvEeScraper implements RentalScraper {

    private static final String URL = "https://www.kv.ee/?deal_type=2&county=18&parish=1061&property_type=1";
    private static final String BASE_URL = "https://www.kv.ee";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public String getSourceName() {
        return "KV_EE";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();

        try {
            log.info("Starting KV.ee scrape from {}", URL);
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .get();

            // KV.ee renders results inside article elements with class "object-type-apartment"
            Elements items = doc.select("article.object-type-apartment, article.object-type-house");

            if (items.isEmpty()) {
                // fallback — sometimes results are in div rows
                items = doc.select("div.result-row");
            }

            log.info("KV.ee: found {} listing elements", items.size());

            for (Element item : items) {
                try {
                    Listing listing = parseItem(item);
                    if (listing != null) {
                        listings.add(listing);
                    }
                } catch (Exception e) {
                    log.warn("KV.ee: failed to parse one listing item — {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("KV.ee scrape failed: {}", e.getMessage());
        }

        log.info("KV.ee scrape complete — {} listings parsed", listings.size());
        return listings;
    }

    private Listing parseItem(Element item) {
        // extract the listing URL and derive externalId from it
        String href = item.select("a[href]").attr("href");
        if (href == null || href.isBlank()) return null;

        String url = href.startsWith("http") ? href : BASE_URL + href;
        String externalId = extractExternalId(href);
        if (externalId == null) return null;

        String title = item.select("h2, h3, .object-title, .title").text();
        if (title.isBlank()) {
            title = item.select("a[href]").attr("title");
        }

        BigDecimal price = parsePrice(
                item.select(".price, .object-price, span[class*=price]").text()
        );

        BigDecimal size = parseSize(
                item.select(".area, .object-area, span[class*=area], span[class*=size]").text()
        );

        Integer rooms = parseRooms(
                item.select(".rooms, .object-rooms, span[class*=room]").text()
        );

        String address = item.select(".address, .object-address, span[class*=address]").text();
        String neighborhood = detectNeighborhood(address + " " + title);

        return Listing.builder()
                .source(Source.KV_EE)
                .externalId(externalId)
                .title(title.isBlank() ? "KV.ee listing" : title)
                .price(price)
                .size(size)
                .rooms(rooms)
                .neighborhood(neighborhood)
                .street(address)
                .city("Tartu")
                .url(url)
                .build();
    }

    private String extractExternalId(String href) {
        // href is like /kinnisvara/korterid/123456 or /123456
        String[] parts = href.replaceAll("[^0-9/]", "").split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isBlank()) return parts[i];
        }
        return null;
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9,.]", "")
                    .replace(",", ".");
            if (cleaned.isBlank()) return null;
            // take the first numeric part if multiple numbers exist
            String first = cleaned.split("\\.")[0];
            return new BigDecimal(first);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseSize(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9.,]", "")
                    .replace(",", ".");
            if (cleaned.isBlank()) return null;
            return new BigDecimal(cleaned.split(" ")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseRooms(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.isBlank()) return null;
            return Integer.parseInt(digits.substring(0, 1));
        } catch (Exception e) {
            return null;
        }
    }

    private String detectNeighborhood(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        String[] neighborhoods = {
                "kesklinn", "ülejõe", "tammelinn", "annelinn",
                "karlova", "veeriku", "tähtvere", "supilinn", "ränilinn", "maarjamõisa"
        };
        for (String n : neighborhoods) {
            if (lower.contains(n)) {
                return capitalize(n);
            }
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
