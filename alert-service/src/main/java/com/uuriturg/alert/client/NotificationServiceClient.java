package com.uuriturg.alert.client;

import com.uuriturg.alert.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class NotificationServiceClient {

    private final WebClient notificationWebClient;

    public NotificationServiceClient(@Qualifier("notificationWebClient") WebClient notificationWebClient) {
        this.notificationWebClient = notificationWebClient;
    }

    public void sendNotification(NotificationRequest request) {
        try {
            notificationWebClient.post()
                    .uri("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.info("Notification sent to {}", request.getRecipientEmail());
        } catch (Exception e) {
            log.warn("NotificationServiceClient: failed to send notification to {}: {}",
                    request.getRecipientEmail(), e.getMessage());
        }
    }
}
