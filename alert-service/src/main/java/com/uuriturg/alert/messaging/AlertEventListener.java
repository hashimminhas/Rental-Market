package com.uuriturg.alert.messaging;

import com.uuriturg.alert.dto.ListingEventDto;
import com.uuriturg.alert.service.AlertMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventListener {

    private final AlertMatchService alertMatchService;

    @RabbitListener(queues = "${messaging.queues.alert-listing}")
    public void onListingNew(ListingEventDto event) {
        log.info("Received listing.new event — listingId={} neighborhood={} price={}",
                event.getListingId(), event.getNeighborhood(), event.getPrice());
        try {
            alertMatchService.evaluateAndMatch(event);
        } catch (Exception e) {
            log.error("Error processing listing.new event for listingId={}: {}",
                    event.getListingId(), e.getMessage());
        }
    }
}
