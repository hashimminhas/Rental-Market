package com.uuriturg.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for sending a notification directly via REST")
public class SendNotificationRequest {

    @Schema(description = "User ID of the recipient (optional — use recipientEmail for email-only alerts)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID recipientUserId;

    @Schema(description = "Direct recipient email — used when no userId is available", example = "you@example.com")
    private String recipientEmail;

    @NotBlank
    @Schema(description = "Delivery channel: EMAIL or IN_APP", example = "EMAIL")
    private String channel;

    @NotBlank
    @Schema(description = "Notification subject line", example = "New matching listing found!")
    private String subject;

    @NotBlank
    @Schema(description = "Notification body text")
    private String body;
}
