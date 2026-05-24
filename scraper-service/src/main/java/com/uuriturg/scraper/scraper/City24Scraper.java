package com.uuriturg.scraper.scraper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class City24Scraper implements RentalScraper {

    private static final String API_URL =
            "https://api.city24.ee/et_EE/search/realties?tsType=rent&unitType=Apartment&itemsPerPage=200&address%5Bcounty%5D%5B0%5D=20269";
    private static final String LISTING_BASE = "https://www.city24.ee/en/real-estate/";
    private static final String[] IMAGE_FORMATS = {
        "24", "25", "18",
        "em800x600c", "em550x400c", "800x600c", "400x300c", "em"
    };
    private static volatile String IMAGE_FORMAT = null;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "CITY24";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();
        try {
            log.info("City24: calling JSON API {}", API_URL);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            log.info("City24 API status: {}", resp.statusCode());

            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                // Response is either an array or {"realties": [...]}
                JsonNode items = root.isArray() ? root : root.path("realties");
                if (items.isMissingNode()) items = root;

                if (IMAGE_FORMAT == null) {
                    discoverImageFormat(items);
                }

                log.info("City24: {} total items from API", items.size());
                for (JsonNode item : items) {
                    try {
                        Listing l = parseApiItem(item);
                        if (l != null) listings.add(l);
                    } catch (Exception e) {
                        log.warn("City24: parse error — {}", e.getMessage());
                    }
                }
                log.info("City24: {} Tartu listings after filter", listings.size());
            }
        } catch (Exception e) {
            log.error("City24 API scrape failed: {}", e.getMessage());
        }

        return listings;
    }

    private static String resolveImageUrl(JsonNode n) {
        if (IMAGE_FORMAT == null) return null;
        JsonNode mainImage = n.path("main_image").path("url");
        if (!mainImage.isMissingNode() && !mainImage.isNull()) {
            return mainImage.asText("").replace("{fmt:em}", IMAGE_FORMAT);
        }
        JsonNode images = n.path("images");
        if (images.isArray() && images.size() > 0) {
            return images.get(0).asText("").replace("{fmt:em}", IMAGE_FORMAT);
        }
        return null;
    }

    private void discoverImageFormat(JsonNode items) {
        String candidate = findCandidateImageUrl(items);
        if (candidate == null || candidate.isBlank()) return;

        for (String fmt : IMAGE_FORMATS) {
            String url = candidate.replace("{fmt:em}", fmt);
            if (probeImageUrl(url)) {
                IMAGE_FORMAT = fmt;
                log.info("City24 image format set to {}", fmt);
                return;
            }
        }
    }

    private String findCandidateImageUrl(JsonNode items) {
        if (items == null || !items.isArray()) return null;
        for (JsonNode item : items) {
            JsonNode main = item.path("main_image").path("url");
            if (!main.isMissingNode() && !main.isNull() && !main.asText("").isBlank()) {
                return main.asText("");
            }
            JsonNode images = item.path("images");
            if (images.isArray() && images.size() > 0) {
                String url = images.get(0).asText("");
                if (!url.isBlank()) return url;
            }
        }
        return null;
    }

    private boolean probeImageUrl(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            HttpResponse<Void> resp = http.send(req, HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.debug("City24 image HEAD failed for {}: {}", url, e.getMessage());
            return false;
        }
    }

    private Listing parseApiItem(JsonNode n) {
        JsonNode addr = n.path("address");

        long id = n.path("id").asLong(0);
        if (id == 0) return null;

        String friendlyId = n.path("friendly_id").asText("");
        String url = friendlyId.isBlank()
                ? "https://www.city24.ee/en/real-estate-search/apartments-for-rent/tartu"
                : LISTING_BASE + friendlyId;

        String street      = addr.path("street_name").asText("").trim();
        String houseNumber = addr.path("house_number").asText("").trim();
        String fullStreet  = (street + (houseNumber.isBlank() ? "" : " " + houseNumber)).trim();

        String district    = addr.path("district_name").asText("").trim();
        String neighborhood = district.isBlank() ? detectNeighborhood(fullStreet) : capitalize(district);

        double priceVal = n.path("price").asDouble(0);
        double sizeVal  = n.path("property_size").asDouble(0);
        int rooms       = n.path("room_count").asInt(0);
        double lat      = n.path("latitude").asDouble(0);
        double lng      = n.path("longitude").asDouble(0);

        String title = rooms + "-room apartment" + (neighborhood != null ? " in " + neighborhood : "")
                + (fullStreet.isBlank() ? "" : ", " + fullStreet);

        return Listing.builder()
                .source(Source.CITY24)
                .externalId("city24-" + id)
                .title(title)
                .price(priceVal > 0 ? BigDecimal.valueOf(priceVal) : null)
                .size(sizeVal > 0 ? BigDecimal.valueOf(sizeVal) : null)
                .rooms(rooms > 0 ? rooms : null)
                .neighborhood(neighborhood)
                .street(fullStreet.isBlank() ? null : fullStreet)
                .city("Tartu")
                .url(url)
                .imageUrl(resolveImageUrl(n))
                .latitude(lat != 0 ? lat : null)
                .longitude(lng != 0 ? lng : null)
                .synthetic(false)
                .build();
    }

    private String detectNeighborhood(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        String[] hoods = {"kesklinn", "ülejõe", "tammelinn", "annelinn",
                "karlova", "veeriku", "tähtvere", "supilinn", "ränilinn", "maarjamõisa"};
        for (String n : hoods) {
            if (lower.contains(n)) return capitalize(n);
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
