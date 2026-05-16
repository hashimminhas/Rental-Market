package com.uuriturg.scraper.messaging;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.dto.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${messaging.exchange.listings}")
    private String exchange;

    @Value("${messaging.routing-keys.listing-new}")
    private String routingKey;

    public void publishListingNew(Listing listing) {
        ListingEvent event = ListingEvent.builder()
                .listingId(listing.getListingId())
                .title(listing.getTitle())
                .price(listing.getPrice())
                .neighborhood(listing.getNeighborhood())
                .size(listing.getSize())
                .rooms(listing.getRooms())
                .url(listing.getUrl())
                .scrapedAt(listing.getScrapedAt())
                .build();

        amqpTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Published listing.new event for listingId={} neighborhood={}",
                listing.getListingId(), listing.getNeighborhood());
    }
}
