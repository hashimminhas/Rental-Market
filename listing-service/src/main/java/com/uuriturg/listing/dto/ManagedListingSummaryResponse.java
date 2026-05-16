package com.uuriturg.listing.dto;

import com.uuriturg.listing.domain.ManagedListingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight managed listing summary for list views")
public class ManagedListingSummaryResponse {

    @Schema(description = "Managed listing UUID")
    private UUID managedListingId;

    @Schema(description = "Landlord profile ID")
    private UUID landlordId;

    @Schema(description = "Listing title", example = "Cosy 2-room flat in Kesklinn")
    private String title;

    @Schema(description = "Tartu neighborhood", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Monthly rent in EUR", example = "550.00")
    private BigDecimal price;

    @Schema(description = "Number of rooms", example = "2")
    private Integer rooms;

    @Schema(description = "Listing status", example = "AVAILABLE")
    private ManagedListingStatus status;

    @Schema(description = "When the landlord claimed this listing")
    private LocalDateTime claimedAt;
}
