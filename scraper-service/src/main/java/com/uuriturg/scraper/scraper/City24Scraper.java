package com.uuriturg.scraper.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class City24Scraper implements RentalScraper {

    private static final String API_URL =
            "https://api.city24.ee/et_EE/search/realties?tsType=rent&unitType=Apartment&itemsPerPage=200";
    private static final String LISTING_BASE = "https://www.city24.ee/en/real-estate/";

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

    private Listing parseApiItem(JsonNode n) {
        JsonNode addr = n.path("address");
        String county = addr.path("county_name").asText("");
        String city   = addr.path("city_name").asText("");

        // Filter: only Tartu area
        if (!county.contains("Tartu") && !city.equalsIgnoreCase("Tartu")) return null;

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
        String neighborhood = district.isBlank() ? detectNeighborhood(fullStreet + " " + city) : capitalize(district);

        double priceVal = n.path("price").asDouble(0);
        double sizeVal  = n.path("property_size").asDouble(0);
        int rooms       = n.path("room_count").asInt(0);

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
                .build();
    }

    public List<Listing> generateSeedListings() {
        Random rng = new Random(55);
        List<Listing> seed = new ArrayList<>();
        String[][] data = {
            {"Kesklinn",    "Küütri",       "2", "680", "980",  "50", "72"},
            {"Kesklinn",    "Raekoja",      "1", "520", "750",  "34", "50"},
            {"Tammelinn",   "Tammela",      "3", "720", "1050", "68", "94"},
            {"Tammelinn",   "Näituse",      "2", "580", "840",  "54", "76"},
            {"Karlova",     "Kastani",      "2", "600", "870",  "55", "77"},
            {"Karlova",     "Tähe",         "1", "440", "640",  "36", "52"},
            {"Annelinn",    "Kaunase",      "2", "360", "540",  "52", "72"},
            {"Annelinn",    "Mõisavahe",    "3", "450", "660",  "64", "90"},
            {"Supilinn",    "Oa",           "2", "640", "930",  "55", "77"},
            {"Veeriku",     "Veeriku",      "2", "520", "760",  "55", "78"},
            {"Tähtvere",    "Tähtvere",     "2", "560", "820",  "58", "80"},
            {"Maarjamõisa", "Maarjamõisa",  "2", "550", "800",  "57", "80"},
        };
        for (int i = 0; i < data.length; i++) {
            String[] r = data[i];
            int price = Integer.parseInt(r[3]) + rng.nextInt(Integer.parseInt(r[4]) - Integer.parseInt(r[3]) + 1);
            int size  = Integer.parseInt(r[5]) + rng.nextInt(Integer.parseInt(r[6]) - Integer.parseInt(r[5]) + 1);
            int rooms = Integer.parseInt(r[2]);
            int num   = 1 + rng.nextInt(28);
            seed.add(Listing.builder()
                    .source(Source.CITY24)
                    .externalId("city24-" + (10000 + i))
                    .title(rooms + "-toaline korter " + r[0] + "s, " + r[1] + " " + num)
                    .price(BigDecimal.valueOf(price))
                    .size(BigDecimal.valueOf(size))
                    .rooms(rooms)
                    .neighborhood(r[0])
                    .street(r[1] + " " + num)
                    .city("Tartu")
                    .url("https://www.city24.ee/en/real-estate-search/apartments-for-rent/tartu")
                    .synthetic(true)
                    .build());
        }
        log.info("Generated {} City24 seed listings", seed.size());
        return seed;
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
