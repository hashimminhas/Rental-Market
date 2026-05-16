package com.uuriturg.notification.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingClaimedEventDto {

    private UUID managedListingId;
    private UUID scrapedListingId;
    private UUID landlordId;
    private String landlordDisplayName;
    private String title;
    private String neighborhood;
    private BigDecimal price;
    private LocalDateTime claimedAt;
}
