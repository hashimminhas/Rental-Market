package com.uuriturg.listing.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordValidateDto {

    private UUID landlordId;
    private UUID userId;
    private String displayName;
    private Boolean isVerified;
}
