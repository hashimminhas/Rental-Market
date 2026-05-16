package com.uuriturg.notification.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordProfileDto {

    private UUID landlordId;
    private UUID userId;
    private String displayName;
    private Boolean isVerified;
}
