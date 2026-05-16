package com.uuriturg.listing.messaging;

import com.uuriturg.listing.domain.ManagedListing;
import com.uuriturg.listing.dto.ListingClaimedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingClaimedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${messaging.exchange.listings}")
    private String exchange;

    @Value("${messaging.routing-keys.listing-claimed}")
    private String routingKey;

    public void publishListingClaimed(ManagedListing listing, String landlordDisplayName) {
        try {
            ListingClaimedEventDto event = ListingClaimedEventDto.builder()
                    .managedListingId(listing.getManagedListingId())
                    .scrapedListingId(listing.getScrapedListingId())
                    .landlordId(listing.getLandlordId())
                    .landlordDisplayName(landlordDisplayName)
                    .title(listing.getTitle())
                    .neighborhood(listing.getNeighborhood())
                    .price(listing.getPrice())
                    .claimedAt(listing.getClaimedAt())
                    .build();
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published listing.claimed event for managedListing={}", listing.getManagedListingId());
        } catch (Exception e) {
            log.warn("Failed to publish listing.claimed event: {}", e.getMessage());
        }
    }
}
