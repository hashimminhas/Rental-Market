package com.uuriturg.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Latest price snapshot for one Tartu neighborhood")
public class NeighborhoodSummaryResponse {

    @Schema(description = "Neighborhood name", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Date this snapshot was computed")
    private LocalDate snapshotDate;

    @Schema(description = "Average monthly rent in EUR across all active listings", example = "580.00")
    private BigDecimal averagePrice;

    @Schema(description = "Average price per m² in EUR", example = "12.50")
    private BigDecimal averagePricePerSqm;

    @Schema(description = "Median monthly rent in EUR", example = "560.00")
    private BigDecimal medianPrice;

    @Schema(description = "Number of active listings in this neighborhood", example = "14")
    private Integer listingCount;

    @Schema(description = "Percentage price change vs the previous snapshot (null if first snapshot)", example = "2.30")
    private BigDecimal priceChangePercent;
}
