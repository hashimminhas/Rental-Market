package com.uuriturg.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full neighborhood profile including reviews and live price data")
public class NeighborhoodResponse {

    @Schema(description = "Neighborhood UUID")
    private UUID neighborhoodId;

    @Schema(description = "Display name", example = "Kesklinn")
    private String name;

    @Schema(description = "URL-friendly slug", example = "kesklinn")
    private String slug;

    @Schema(description = "Description of the neighborhood")
    private String description;

    @Schema(description = "Distance from city centre in km", example = "0.5")
    private Double distanceToCenter;

    @Schema(description = "Comma-separated tags", example = "lively,central,walkable")
    private String characteristics;

    @Schema(description = "Average resident rating (1–5)", example = "4.2")
    private Double averageRating;

    @Schema(description = "Total number of reviews", example = "12")
    private Integer reviewCount;

    @Schema(description = "Average monthly rent from analytics-service (EUR)", example = "520.00")
    private BigDecimal averagePrice;

    @Schema(description = "Average price per m² from analytics-service (EUR)", example = "9.50")
    private BigDecimal averagePricePerSqm;

    @Schema(description = "Active listing count from analytics-service", example = "34")
    private Integer listingCount;

    @Schema(description = "When the neighborhood profile was created")
    private LocalDateTime createdAt;
}
