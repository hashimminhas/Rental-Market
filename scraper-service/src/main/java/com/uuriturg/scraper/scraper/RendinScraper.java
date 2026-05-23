package com.uuriturg.scraper.scraper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RendinScraper implements RentalScraper {

    private static final String API_URL =
            "https://europe-west1-rendin-production.cloudfunctions.net/getSearchApartments";
    private static final String SEARCH_URL = "https://rendin.ee/et";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int MAX_RESULTS = 50;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "RENDIN";
    }

    @Override
    public List<Listing> scrape() {
        List<Listing> listings = new ArrayList<>();

        try {
            String requestBody = buildSearchRequestBody();
            log.info("Rendin: calling Firebase callable API {}", API_URL);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Origin", "https://rendin.ee")
                    .header("Referer", SEARCH_URL)
                    .header("User-Agent", USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Rendin API status: {}", resp.statusCode());

            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                JsonNode items = root.path("result").path("foundApartments");

                if (items.isArray()) {
                    log.info("Rendin: {} apartments returned by API", items.size());
                    for (JsonNode item : items) {
                        try {
                            Listing listing = parseApiItem(item);
                            if (listing != null) listings.add(listing);
                        } catch (Exception e) {
                            log.warn("Rendin: parse error - {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("Rendin API response did not contain result.foundApartments");
                }
            } else {
                log.warn("Rendin API returned status {} with body: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.error("Rendin API scrape failed: {}", e.getMessage());
        }

        log.info("Rendin scrape complete - {} listings parsed", listings.size());
        return listings;
    }

    private String buildSearchRequestBody() throws Exception {
        ObjectNode data = mapper.createObjectNode();
        data.put("country", "EE");
        data.put("city", "Tartu");
        data.put("maxReturn", MAX_RESULTS);
        data.set("districts", mapper.createArrayNode());

        ArrayNode propertyTypes = mapper.createArrayNode();
        propertyTypes.add("APARTMENT");
        data.set("propertyTypes", propertyTypes);

        ObjectNode body = mapper.createObjectNode();
        body.set("data", data);
        return mapper.writeValueAsString(body);
    }

    private Listing parseApiItem(JsonNode item) {
        String city = item.path("city").asText("");
        if (!"Tartu".equalsIgnoreCase(city)) return null;

        String propertyType = item.path("propertyType").asText("");
        if (!propertyType.isBlank() && !"APARTMENT".equalsIgnoreCase(propertyType)) return null;

        String invitationCode = item.path("invitationCode").asText("");
        String link = item.path("link").asText("");
        if (invitationCode.isBlank()) {
            invitationCode = extractInvitationCode(link);
        }
        if (invitationCode == null || invitationCode.isBlank()) return null;

        String address = item.path("address").asText("").trim();
        Integer rooms = item.path("rooms").isNumber() ? item.path("rooms").asInt() : null;
        String imageUrl = extractImageUrl(item);
        String title = (rooms != null ? rooms + "-toaline korter" : "Korter")
                + " Tartus"
                + (address.isBlank() ? "" : ", " + address);

        return Listing.builder()
                .source(Source.RENDIN)
                .externalId("rendin-" + invitationCode)
                .title(title)
                .price(decimal(item.path("price")))
                .size(decimal(item.path("objectArea")))
                .rooms(rooms)
                .neighborhood(detectNeighborhood(address + " " + title))
                .street(address.isBlank() ? null : address)
                .city("Tartu")
                .url(link.isBlank() ? SEARCH_URL : link)
                .imageUrl(imageUrl)
                .synthetic(false)
                .build();
    }

    private String extractImageUrl(JsonNode item) {
        String direct = text(item, "imageUrl", "mainImage", "coverImage", "thumbnail", "cover");
        if (direct != null) return direct;

        String fromImages = firstUrlFromArray(item.path("images"));
        if (fromImages != null) return fromImages;

        String fromPhotos = firstUrlFromArray(item.path("photos"));
        if (fromPhotos != null) return fromPhotos;

        String fromGallery = firstUrlFromArray(item.path("gallery"));
        if (fromGallery != null) return fromGallery;

        return null;
    }

    private String firstUrlFromArray(JsonNode node) {
        if (node == null || !node.isArray() || node.size() == 0) return null;
        for (JsonNode entry : node) {
            if (entry.isTextual()) {
                String url = entry.asText("").trim();
                if (!url.isBlank()) return url;
            }
            String url = text(entry, "url", "imageUrl", "src", "href");
            if (url != null) return url;
        }
        return null;
    }

    private String text(JsonNode node, String... keys) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        for (String key : keys) {
            String val = node.path(key).asText("").trim();
            if (!val.isBlank()) return val;
        }
        return null;
    }

    private String extractInvitationCode(String link) {
        if (link == null || link.isBlank()) return null;
        int slash = link.lastIndexOf('/');
        return slash >= 0 ? link.substring(slash + 1).trim() : link.trim();
    }

    private BigDecimal decimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        if (node.isNumber()) return node.decimalValue();
        String text = node.asText("");
        if (text.isBlank()) return null;
        try {
            return new BigDecimal(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String detectNeighborhood(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String n : new String[]{
                "kesklinn", "ülejõe", "tammelinn", "annelinn", "karlova",
                "veeriku", "tähtvere", "supilinn", "ränilinn", "maarjamõisa"
        }) {
            if (lower.contains(n)) {
                return n.substring(0, 1).toUpperCase(Locale.ROOT) + n.substring(1);
            }
        }
        return null;
    }
}
