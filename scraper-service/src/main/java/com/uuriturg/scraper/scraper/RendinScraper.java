package com.uuriturg.scraper.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
import java.util.Random;

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
                .synthetic(false)
                .build();
    }

    public List<Listing> generateSeedListings() {
        Random rng = new Random(42);
        List<Listing> seed = new ArrayList<>();

        String[][] data = {
            // { neighborhood, street, rooms, minPrice, maxPrice, minSize, maxSize }
            {"Kesklinn",    "Küütri",       "2", "700", "1050", "50", "70"},
            {"Kesklinn",    "Raekoja",      "1", "550", "780",  "34", "48"},
            {"Kesklinn",    "Ülikooli",     "3", "900", "1350", "72", "98"},
            {"Tammelinn",   "Tammela",      "2", "620", "900",  "55", "75"},
            {"Tammelinn",   "Näituse",      "3", "750", "1100", "68", "92"},
            {"Tammelinn",   "Filosoofi",    "1", "450", "650",  "36", "52"},
            {"Karlova",     "Kastani",      "2", "640", "920",  "56", "76"},
            {"Karlova",     "Tähe",         "1", "460", "660",  "37", "52"},
            {"Karlova",     "Aleksandri",   "3", "780", "1150", "72", "98"},
            {"Supilinn",    "Oa",           "2", "650", "950",  "54", "76"},
            {"Supilinn",    "Kartuli",      "1", "470", "680",  "38", "54"},
            {"Supilinn",    "Herne",        "3", "800", "1180", "70", "96"},
            {"Veeriku",     "Veeriku",      "2", "540", "780",  "55", "75"},
            {"Veeriku",     "Männiku",      "3", "660", "960",  "68", "95"},
            {"Tähtvere",    "Tähtvere",     "2", "590", "850",  "58", "80"},
            {"Tähtvere",    "Lepiku",       "1", "430", "620",  "38", "54"},
            {"Annelinn",    "Kaunase",      "2", "380", "560",  "52", "72"},
            {"Annelinn",    "Mõisavahe",    "3", "460", "680",  "65", "90"},
            {"Maarjamõisa", "Maarjamõisa",  "2", "560", "820",  "58", "82"},
            {"Ränilinn",    "Ringtee",      "2", "500", "730",  "54", "76"},
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
            int streetNum = 1 + rng.nextInt(28);

            String title = rooms + "-toaline korter " + neighborhood + "s, " + street + " " + streetNum;
            String externalId = "rendin-" + (30000 + i);

            seed.add(Listing.builder()
                    .source(Source.RENDIN)
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

        log.info("Generated {} Rendin seed listings", seed.size());
        return seed;
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
