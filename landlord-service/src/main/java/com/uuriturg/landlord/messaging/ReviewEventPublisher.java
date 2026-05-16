package com.uuriturg.landlord.messaging;

import com.uuriturg.landlord.domain.LandlordProfile;
import com.uuriturg.landlord.domain.TenantReview;
import com.uuriturg.landlord.dto.ReviewEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${messaging.exchange.landlord}")
    private String exchange;

    @Value("${messaging.routing-keys.review-posted}")
    private String routingKey;

    public void publishReviewPosted(TenantReview review, LandlordProfile landlord) {
        try {
            ReviewEventDto event = ReviewEventDto.builder()
                    .landlordId(review.getLandlordId())
                    .landlordDisplayName(landlord.getDisplayName())
                    .reviewerUserId(review.getReviewerUserId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .reviewedAt(review.getCreatedAt())
                    .build();
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published review.posted event for landlord={} rating={}", landlord.getLandlordId(), review.getRating());
        } catch (Exception e) {
            log.warn("Failed to publish review.posted event: {}", e.getMessage());
        }
    }
}
