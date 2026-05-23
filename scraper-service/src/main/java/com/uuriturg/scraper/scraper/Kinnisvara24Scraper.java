package com.uuriturg.scraper.scraper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
public class Kinnisvara24Scraper implements RentalScraper {

    private static final String URL = "https://www.kinnisvara24.ee/et/kinnisvaraotsing?liik=uuri&kategooria=korter&asukoht=Tartu";
    private static final String BASE_URL = "https://www.kinnisvara24.ee";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public String getSourceName() {
        return "KINNISVARA24";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();
        try {
            log.info("Starting Kinnisvara24 scrape from {}", URL);
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .get();

            Elements items = doc.select("article.object, div.object-item, li.listing-item, article[class*=listing]");
            if (items.isEmpty()) {
                items = doc.select("div[class*=result] article, section[class*=object] li");
            }

            log.info("Kinnisvara24: found {} elements", items.size());

            for (Element item : items) {
                try {
                    Listing l = parseItem(item);
                    if (l != null) listings.add(l);
                } catch (Exception e) {
                    log.warn("Kinnisvara24: parse error — {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Kinnisvara24 scrape failed: {}", e.getMessage());
        }

        log.info("Kinnisvara24 scrape complete — {} listings", listings.size());

        return listings;
    }

    private Listing parseItem(Element item) {
        String href = item.select("a[href]").attr("href");
        if (href == null || href.isBlank()) return null;

        String url = href.startsWith("http") ? href : BASE_URL + href;
        String externalId = extractId(href);
        if (externalId == null) return null;

        String title   = item.select("h2, h3, [class*=title], [class*=name]").text();
        BigDecimal price = parseNum(item.select("[class*=price]").text());
        BigDecimal size  = parseNum(item.select("[class*=area], [class*=size], [class*=pind]").text());
        Integer rooms    = parseRooms(item.select("[class*=room], [class*=tuba]").text());
        String address   = item.select("[class*=address], [class*=asukoht]").text();

        return Listing.builder()
                .source(Source.KINNISVARA24)
                .externalId(externalId)
                .title(title.isBlank() ? "Kinnisvara24 listing" : title)
                .price(price).size(size).rooms(rooms)
                .neighborhood(detectNeighborhood(address + " " + title))
                .street(address).city("Tartu").url(url)
                .build();
    }

    private String extractId(String href) {
        String[] parts = href.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String seg = parts[i].replaceAll("[^0-9]", "");
            if (seg.length() >= 4) return "k24-" + seg;
        }
        return null;
    }

    private BigDecimal parseNum(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String c = text.replaceAll("[^0-9.,]", "").replace(",", ".");
            if (c.isBlank()) return null;
            return new BigDecimal(c.split("\\.")[0]);
        } catch (Exception e) { return null; }
    }

    private Integer parseRooms(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String d = text.replaceAll("[^0-9]", "");
            return d.isBlank() ? null : Integer.parseInt(d.substring(0, 1));
        } catch (Exception e) { return null; }
    }

    private String detectNeighborhood(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        for (String n : new String[]{"kesklinn","ülejõe","tammelinn","annelinn","karlova","veeriku","tähtvere","supilinn","ränilinn","maarjamõisa"}) {
            if (lower.contains(n)) return n.substring(0, 1).toUpperCase() + n.substring(1);
        }
        return null;
    }
}
