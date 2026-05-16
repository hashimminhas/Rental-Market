package com.uuriturg.notification.messaging;

import com.uuriturg.notification.dto.ReviewPostedEventDto;
import com.uuriturg.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LandlordEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${messaging.queues.notification-landlord}")
    public void onReviewPosted(ReviewPostedEventDto event) {
        log.info("Received review.posted event — landlordId={} rating={}",
                event.getLandlordId(), event.getRating());
        try {
            notificationService.sendFromReviewEvent(event);
        } catch (Exception e) {
            log.error("Error processing review.posted event: {}", e.getMessage());
        }
    }
}
