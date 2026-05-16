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
@Schema(description = "Full managed listing owned by a landlord")
public class ManagedListingResponse {

    @Schema(description = "Managed listing UUID")
    private UUID managedListingId;

    @Schema(description = "Original scraped listing ID from scraper-service")
    private UUID scrapedListingId;

    @Schema(description = "Landlord profile ID that owns this listing")
    private UUID landlordId;

    @Schema(description = "Listing title", example = "Cosy 2-room flat in Kesklinn")
    private String title;

    @Schema(description = "Landlord description of the property")
    private String description;

    @Schema(description = "Monthly rent in EUR", example = "550.00")
    private BigDecimal price;

    @Schema(description = "Size in m²", example = "52.0")
    private BigDecimal size;

    @Schema(description = "Number of rooms", example = "2")
    private Integer rooms;

    @Schema(description = "Tartu neighborhood", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Street address", example = "Raatuse 22, Tartu")
    private String address;

    @Schema(description = "Original scraper URL")
    private String originalUrl;

    @Schema(description = "Listing status", example = "AVAILABLE")
    private ManagedListingStatus status;

    @Schema(description = "When the landlord claimed this listing")
    private LocalDateTime claimedAt;

    @Schema(description = "When the listing was last updated")
    private LocalDateTime updatedAt;
}
