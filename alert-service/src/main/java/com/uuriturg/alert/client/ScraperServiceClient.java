package com.uuriturg.alert.client;

import com.uuriturg.alert.dto.ListingEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ScraperServiceClient {

    private final WebClient scraperWebClient;

    public ScraperServiceClient(@Qualifier("scraperWebClient") WebClient scraperWebClient) {
        this.scraperWebClient = scraperWebClient;
    }

    public List<ListingEventDto> findMatchingListings(String neighborhood, BigDecimal maxPrice, BigDecimal minSize) {
        try {
            StringBuilder uri = new StringBuilder("/listings?");
            if (neighborhood != null) uri.append("neighborhood=").append(neighborhood).append("&");
            if (maxPrice != null) uri.append("maxPrice=").append(maxPrice).append("&");
            if (minSize != null) uri.append("minSize=").append(minSize).append("&");

            List<Map<String, Object>> raw = scraperWebClient.get()
                    .uri(uri.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            if (raw == null) return Collections.emptyList();

            return raw.stream().map(m -> ListingEventDto.builder()
                    .listingId(m.get("listingId") != null ? java.util.UUID.fromString(m.get("listingId").toString()) : java.util.UUID.randomUUID())
                    .title(m.get("title") != null ? m.get("title").toString() : null)
                    .neighborhood(m.get("neighborhood") != null ? m.get("neighborhood").toString() : null)
                    .price(m.get("price") != null ? new BigDecimal(m.get("price").toString()) : null)
                    .size(m.get("size") != null ? new BigDecimal(m.get("size").toString()) : null)
                    .rooms(m.get("rooms") != null ? ((Number) m.get("rooms")).intValue() : null)
                    .url(m.get("url") != null ? m.get("url").toString() : null)
                    .build()).toList();
        } catch (Exception e) {
            log.warn("ScraperServiceClient: failed to fetch listings: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
