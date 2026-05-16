package com.uuriturg.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for a landlord claiming a scraped listing")
public class ClaimListingRequest {

    @NotNull
    @Schema(description = "Landlord profile ID from landlord-service", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID landlordId;

    @NotNull
    @Schema(description = "Scraper listing ID from scraper-service to claim", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID scrapedListingId;

    @Schema(description = "Override title (defaults to scraped title if omitted)", example = "Cosy 2-room flat in Kesklinn")
    private String title;

    @Schema(description = "Landlord's own description of the property")
    private String description;

    @Schema(description = "Override price in EUR/month (defaults to scraped price if omitted)", example = "550.00")
    private BigDecimal price;

    @Schema(description = "Override address")
    private String address;
}
