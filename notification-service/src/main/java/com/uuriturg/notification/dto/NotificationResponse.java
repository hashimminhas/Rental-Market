package com.uuriturg.notification.dto;

import com.uuriturg.notification.domain.NotificationChannel;
import com.uuriturg.notification.domain.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A notification record")
public class NotificationResponse {

    @Schema(description = "Notification UUID")
    private UUID notificationId;

    @Schema(description = "Recipient user ID")
    private UUID recipientUserId;

    @Schema(description = "Recipient email address (resolved from user-service)")
    private String recipientEmail;

    @Schema(description = "Delivery channel", example = "EMAIL")
    private NotificationChannel channel;

    @Schema(description = "Subject line")
    private String subject;

    @Schema(description = "Notification body")
    private String body;

    @Schema(description = "Delivery status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "When the notification was delivered")
    private LocalDateTime sentAt;

    @Schema(description = "When the notification record was created")
    private LocalDateTime createdAt;
}
