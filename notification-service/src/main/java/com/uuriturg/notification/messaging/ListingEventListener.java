package com.uuriturg.notification.messaging;

import com.uuriturg.notification.dto.ListingClaimedEventDto;
import com.uuriturg.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${messaging.queues.notification-listing}")
    public void onListingClaimed(ListingClaimedEventDto event) {
        log.info("Received listing.claimed event — landlordId={} listing={}",
                event.getLandlordId(), event.getTitle());
        try {
            notificationService.sendFromListingEvent(event);
        } catch (Exception e) {
            log.error("Error processing listing.claimed event: {}", e.getMessage());
        }
    }
}
