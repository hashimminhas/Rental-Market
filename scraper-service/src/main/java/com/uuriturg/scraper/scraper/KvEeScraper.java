package com.uuriturg.scraper.scraper;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.zip.GZIPInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class KvEeScraper implements RentalScraper {

    private static final String SEARCH_URL =
            "https://www.kv.ee/search?deal_type=2&county=12&parish=1063&property_type=1";
    private static final String RSS_URL = SEARCH_URL + "&rss=1";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 2;

    private static final Pattern EXTERNAL_ID = Pattern.compile("(\\d+)\\.html(?:$|[?#])");
    private static final Pattern PRICE = Pattern.compile("([0-9][0-9\\s\\u00A0]*)\\s*(?:\\u20AC|EUR)");
    private static final Pattern ROOMS = Pattern.compile("(\\d+)\\s*tuba", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SIZE_AFTER_OWNERSHIP = Pattern.compile(
            "(?:Korteriomand|Kinnistu|Üürileping),\\s*([0-9]+(?:[\\.,][0-9]+)?)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SIZE_WITH_LABEL = Pattern.compile(
            "(?:Pindala|Üldpind)[:\\s]+([0-9]+(?:[\\.,][0-9]+)?)\\s*(?:m2|m²)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String getSourceName() {
        return "KV_EE";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();
        Set<String> seenExternalIds = new HashSet<>();

        try {
            for (int page = 0; page < MAX_PAGES; page++) {
                int start = page * PAGE_SIZE;
                String url = start == 0 ? RSS_URL : RSS_URL + "&start=" + start;
                log.info("KV.ee: calling RSS feed {}", url);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "et-EE,et;q=0.9,en-US;q=0.8,en;q=0.7")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("User-Agent", USER_AGENT)
                        .header("Referer", "https://www.kv.ee/")
                        .header("Cache-Control", "no-cache")
                        .GET()
                        .build();

                HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
                log.info("KV.ee RSS status for start {}: {}", start, resp.statusCode());
                if (resp.statusCode() != 200) {
                    log.warn("KV.ee RSS non-200 for start {}: body={}", start,
                            new String(resp.body(), java.nio.charset.StandardCharsets.UTF_8).substring(0, Math.min(200, resp.body().length)));
                    break;
                }

                byte[] bodyBytes = resp.body();
                String encoding = resp.headers().firstValue("Content-Encoding").orElse("");
                if ("gzip".equalsIgnoreCase(encoding)) {
                    try (InputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes))) {
                        bodyBytes = gzis.readAllBytes();
                    }
                }
                List<Listing> pageListings = parseRss(bodyBytes, seenExternalIds);
                if (pageListings.isEmpty()) {
                    break;
                }
                listings.addAll(pageListings);
            }
        } catch (Exception e) {
            log.error("KV.ee RSS scrape failed: {}", e.getMessage());
        }

        log.info("KV.ee scrape complete - {} listings parsed", listings.size());
        return listings;
    }

    private List<Listing> parseRss(byte[] body, Set<String> seenExternalIds) throws Exception {
        List<Listing> listings = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);

        Document doc = factory.newDocumentBuilder()
                .parse(new InputSource(new ByteArrayInputStream(body)));
        NodeList items = doc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            Listing listing = parseRssItem(item);
            if (listing != null && seenExternalIds.add(listing.getExternalId())) {
                listings.add(listing);
            }
        }

        return listings;
    }

    private Listing parseRssItem(Element item) {
        String title = childText(item, "title");
        String url = childText(item, "guid");
        if (url == null || url.isBlank()) {
            url = firstLinkFromDescription(childText(item, "description"));
        }

        String externalId = extractExternalId(url);
        if (externalId == null) return null;

        String descriptionHtml = childText(item, "description");
        String description = htmlToText(descriptionHtml);
        String combined = (title + " " + description).trim();

        return Listing.builder()
                .source(Source.KV_EE)
                .externalId("kv-" + externalId)
                .title(title)
                .price(parsePrice(title))
                .size(parseSize(description))
                .rooms(parseRooms(title))
                .neighborhood(detectNeighborhood(combined))
                .street(parseStreet(title))
                .city("Tartu")
                .url(url)
                .synthetic(false)
                .build();
    }

    public List<Listing> generateSeedListings() {
        Random rng = new Random(42);
        List<Listing> seed = new ArrayList<>();

        String[][] data = {
            // { neighborhood, street, rooms, minPrice, maxPrice, minSize, maxSize }
            {"Kesklinn",     "Küütri",       "2", "600", "950",  "45", "70"},
            {"Kesklinn",     "Raekoja",      "3", "800", "1200", "65", "90"},
            {"Kesklinn",     "Rüütli",       "1", "450", "650",  "30", "48"},
            {"Kesklinn",     "Vanemuise",    "2", "620", "880",  "50", "68"},
            {"Kesklinn",     "Riia",         "3", "750", "1100", "70", "95"},
            {"Kesklinn",     "Vallikraavi",  "1", "420", "590",  "28", "42"},
            {"Ülejõe",       "Kalda",        "2", "480", "720",  "48", "65"},
            {"Ülejõe",       "Sõbra",        "3", "580", "850",  "60", "85"},
            {"Ülejõe",       "Ujula",        "1", "370", "520",  "32", "45"},
            {"Ülejõe",       "Jaama",        "2", "500", "740",  "50", "70"},
            {"Tammelinn",    "Tammela",      "3", "650", "950",  "65", "90"},
            {"Tammelinn",    "Näituse",      "2", "520", "780",  "50", "72"},
            {"Tammelinn",    "Filosoofi",    "1", "400", "580",  "35", "50"},
            {"Tammelinn",    "Lepp",         "4", "850", "1250", "80", "110"},
            {"Annelinn",     "Kaunase",      "2", "350", "520",  "48", "68"},
            {"Annelinn",     "Pepleri",      "3", "420", "620",  "60", "85"},
            {"Annelinn",     "Ehitajate",    "1", "280", "400",  "30", "45"},
            {"Annelinn",     "Mõisavahe",    "2", "360", "540",  "50", "70"},
            {"Annelinn",     "Kalevi",       "3", "400", "600",  "65", "88"},
            {"Annelinn",     "Anne",         "1", "270", "390",  "28", "42"},
            {"Karlova",      "Kastani",      "2", "580", "820",  "52", "72"},
            {"Karlova",      "Tähe",         "3", "720", "1050", "68", "92"},
            {"Karlova",      "Aleksandri",   "1", "430", "620",  "34", "48"},
            {"Karlova",      "Roosi",        "2", "550", "790",  "50", "68"},
            {"Veeriku",      "Veeriku",      "3", "600", "880",  "65", "90"},
            {"Veeriku",      "Laane",        "2", "480", "700",  "50", "70"},
            {"Veeriku",      "Männiku",      "4", "750", "1100", "85", "115"},
            {"Tähtvere",     "Tähtvere",     "2", "550", "800",  "55", "75"},
            {"Tähtvere",     "Kadaka",       "3", "680", "980",  "68", "95"},
            {"Tähtvere",     "Lepiku",       "1", "410", "580",  "36", "50"},
            {"Supilinn",     "Oa",           "2", "580", "850",  "52", "72"},
            {"Supilinn",     "Kartuli",      "1", "420", "620",  "35", "50"},
            {"Supilinn",     "Herne",        "3", "700", "1050", "65", "90"},
            {"Ränilinn",     "Räni",         "2", "440", "640",  "50", "70"},
            {"Ränilinn",     "Puru",         "3", "520", "760",  "65", "88"},
            {"Maarjamõisa",  "Maarjamõisa",  "2", "520", "760",  "55", "78"},
            {"Maarjamõisa",  "Puiestee",     "3", "640", "940",  "68", "95"},
        };

        for (int i = 0; i < data.length; i++) {
            String[] row = data[i];
            String neighborhood = row[0];
            String street = row[1];
            int rooms = Integer.parseInt(row[2]);
            int minPrice = Integer.parseInt(row[3]);
            int maxPrice = Integer.parseInt(row[4]);
            int minSize = Integer.parseInt(row[5]);
            int maxSize = Integer.parseInt(row[6]);

            int price = minPrice + rng.nextInt(maxPrice - minPrice + 1);
            int size = minSize + rng.nextInt(maxSize - minSize + 1);
            int streetNum = 1 + rng.nextInt(30);

            String title = rooms + "-toaline korter " + neighborhood + "s, " + street + " tän. " + streetNum;
            String externalId = "seed-" + (10000 + i);

            seed.add(Listing.builder()
                    .source(Source.KV_EE)
                    .externalId(externalId)
                    .title(title)
                    .price(BigDecimal.valueOf(price))
                    .size(BigDecimal.valueOf(size))
                    .rooms(rooms)
                    .neighborhood(neighborhood)
                    .street(street + " " + streetNum)
                    .city("Tartu")
                    .url(SEARCH_URL)
                    .synthetic(true)
                    .build());
        }

        log.info("Generated {} seed listings for demo", seed.size());
        return seed;
    }

    private String childText(Element item, String tagName) {
        NodeList nodes = item.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return "";
        return nodes.item(0).getTextContent().trim();
    }

    private String firstLinkFromDescription(String descriptionHtml) {
        if (descriptionHtml == null) return null;
        Matcher matcher = Pattern.compile("href=\"([^\"]+)\"").matcher(descriptionHtml);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractExternalId(String url) {
        if (url == null || url.isBlank()) return null;
        Matcher matcher = EXTERNAL_ID.matcher(url);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = PRICE.matcher(text);
        if (!matcher.find()) return null;
        String digits = matcher.group(1).replaceAll("[^0-9]", "");
        return digits.isBlank() ? null : new BigDecimal(digits);
    }

    private BigDecimal parseSize(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = SIZE_AFTER_OWNERSHIP.matcher(text);
        boolean matched = matcher.find();
        if (!matched) {
            matcher = SIZE_WITH_LABEL.matcher(text);
            matched = matcher.find();
        }
        if (!matched) return null;

        try {
            return new BigDecimal(matcher.group(1).replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseRooms(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = ROOMS.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private String parseStreet(String title) {
        if (title == null || title.isBlank()) return null;
        String[] parts = title.split(",");
        int roomsIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (ROOMS.matcher(parts[i]).find()) {
                roomsIndex = i;
                break;
            }
        }
        if (roomsIndex < 0) return null;

        for (int i = roomsIndex + 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isBlank()) continue;
            String lower = part.toLowerCase(Locale.ROOT);
            if (lower.contains("tartu") || PRICE.matcher(part).find()) {
                break;
            }
            return part;
        }
        return null;
    }

    private String htmlToText(String html) {
        if (html == null || html.isBlank()) return "";
        return html
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&amp;", "&")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String detectNeighborhood(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase(Locale.ROOT);
        String[] neighborhoods = {
                "kesklinn", "ülejõe", "tammelinn", "annelinn",
                "karlova", "veeriku", "tähtvere", "supilinn", "ränilinn", "maarjamõisa"
        };
        for (String n : neighborhoods) {
            if (lower.contains(n)) return capitalize(n);
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
