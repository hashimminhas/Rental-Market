package com.uuriturg.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight neighborhood summary for list views")
public class NeighborhoodSummaryResponse {

    @Schema(description = "Neighborhood UUID")
    private UUID neighborhoodId;

    @Schema(description = "Display name", example = "Annelinn")
    private String name;

    @Schema(description = "URL-friendly slug", example = "annelinn")
    private String slug;

    @Schema(description = "Distance from city centre in km", example = "3.2")
    private Double distanceToCenter;

    @Schema(description = "Average resident rating (1–5)", example = "3.8")
    private Double averageRating;

    @Schema(description = "Number of reviews", example = "7")
    private Integer reviewCount;
}
