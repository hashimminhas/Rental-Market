package com.uuriturg.listing.client;

import com.uuriturg.listing.dto.ScraperListingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@Slf4j
public class ScraperClient {

    private final WebClient scraperWebClient;

    public ScraperClient(@Qualifier("scraperWebClient") WebClient scraperWebClient) {
        this.scraperWebClient = scraperWebClient;
    }

    public ScraperListingDto getListingById(UUID listingId) {
        try {
            return scraperWebClient.get()
                    .uri("/listings/{listingId}", listingId)
                    .retrieve()
                    .bodyToMono(ScraperListingDto.class)
                    .block();
        } catch (Exception e) {
            log.warn("Could not fetch scraper listing {}: {}", listingId, e.getMessage());
            return null;
        }
    }
}
