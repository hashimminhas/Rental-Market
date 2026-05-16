package com.uuriturg.alert.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private UUID recipientUserId;
    private String channel;
    private String subject;
    private String body;
}
