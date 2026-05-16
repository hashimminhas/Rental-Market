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
public class City24Scraper implements RentalScraper {

    private static final String URL = "https://city24.ee/en/real-estate-search/apartments-for-rent/tartu";
    private static final String BASE_URL = "https://city24.ee";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public String getSourceName() {
        return "CITY24";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();

        try {
            log.info("Starting City24 scrape from {}", URL);
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .get();

            // City24 renders listings as article elements inside a results container
            Elements items = doc.select("article.object-type-apartment, article[class*=listing], li[class*=listing]");

            if (items.isEmpty()) {
                items = doc.select("div[class*=result] article, section[class*=list] article");
            }

            log.info("City24: found {} listing elements", items.size());

            for (Element item : items) {
                try {
                    Listing listing = parseItem(item);
                    if (listing != null) {
                        listings.add(listing);
                    }
                } catch (Exception e) {
                    log.warn("City24: failed to parse one listing item — {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("City24 scrape failed: {}", e.getMessage());
        }

        log.info("City24 scrape complete — {} listings parsed", listings.size());
        return listings;
    }

    private Listing parseItem(Element item) {
        String href = item.select("a[href]").attr("href");
        if (href == null || href.isBlank()) return null;

        String url = href.startsWith("http") ? href : BASE_URL + href;
        String externalId = extractExternalId(href);
        if (externalId == null) return null;

        String title = item.select("h2, h3, [class*=title], [class*=name]").text();

        BigDecimal price = parsePrice(
                item.select("[class*=price]").text()
        );

        BigDecimal size = parseSize(
                item.select("[class*=area], [class*=size], [class*=sqm]").text()
        );

        Integer rooms = parseRooms(
                item.select("[class*=room]").text()
        );

        String address = item.select("[class*=address], [class*=location]").text();
        String neighborhood = detectNeighborhood(address + " " + title);

        return Listing.builder()
                .source(Source.CITY24)
                .externalId(externalId)
                .title(title.isBlank() ? "City24 listing" : title)
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
        // City24 URLs look like /en/real-estate/apartment-12345678 or contain a numeric segment
        String[] parts = href.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String segment = parts[i].replaceAll("[^0-9]", "");
            if (segment.length() >= 4) return segment;
        }
        return null;
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9]", "");
            if (cleaned.isBlank()) return null;
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseSize(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9.,]", "").replace(",", ".");
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
