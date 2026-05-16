package com.uuriturg.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A cheap rental listing returned by the cheapest endpoint")
public class CheapestListingResponse {

    @Schema(description = "Listing UUID from scraper-service")
    private UUID listingId;

    @Schema(description = "Listing title", example = "Studio in Annelinn")
    private String title;

    @Schema(description = "Monthly rent in EUR", example = "280.00")
    private BigDecimal price;

    @Schema(description = "Apartment size in m²", example = "22.00")
    private BigDecimal size;

    @Schema(description = "Price per m² in EUR", example = "12.73")
    private BigDecimal pricePerSqm;

    @Schema(description = "Number of rooms", example = "1")
    private Integer rooms;

    @Schema(description = "Neighborhood name", example = "Annelinn")
    private String neighborhood;

    @Schema(description = "Direct link to the listing on the source portal")
    private String url;
}
