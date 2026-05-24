package com.uuriturg.alert.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String recipientEmail;
    private String channel;
    private String subject;
    private String body;
}
