package com.uuriturg.listing.client;

import com.uuriturg.listing.dto.LandlordValidateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@Slf4j
public class LandlordClient {

    private final WebClient landlordWebClient;

    public LandlordClient(@Qualifier("landlordWebClient") WebClient landlordWebClient) {
        this.landlordWebClient = landlordWebClient;
    }

    public LandlordValidateDto getLandlordById(UUID landlordId) {
        try {
            return landlordWebClient.get()
                    .uri("/landlords/{landlordId}", landlordId)
                    .retrieve()
                    .bodyToMono(LandlordValidateDto.class)
                    .block();
        } catch (Exception e) {
            log.warn("Could not fetch landlord {}: {}", landlordId, e.getMessage());
            return null;
        }
    }
}
