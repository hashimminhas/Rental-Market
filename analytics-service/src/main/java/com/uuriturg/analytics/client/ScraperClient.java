package com.uuriturg.analytics.client;

import com.uuriturg.analytics.dto.ScraperListingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScraperClient {

    private final WebClient scraperWebClient;

    public List<ScraperListingDto> getListingsByNeighborhood(String neighborhood) {
        try {
            List<ScraperListingDto> result = scraperWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/listings")
                            .queryParam("neighborhood", neighborhood)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ScraperListingDto>>() {})
                    .block();
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.warn("ScraperClient: failed to fetch listings for neighborhood '{}': {}", neighborhood, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ScraperListingDto> getListings(String neighborhood, BigDecimal maxPrice) {
        try {
            List<ScraperListingDto> result = scraperWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/listings");
                        if (neighborhood != null && !neighborhood.isBlank())
                            builder = builder.queryParam("neighborhood", neighborhood);
                        if (maxPrice != null)
                            builder = builder.queryParam("maxPrice", maxPrice);
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ScraperListingDto>>() {})
                    .block();
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.warn("ScraperClient: failed to fetch listings: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
