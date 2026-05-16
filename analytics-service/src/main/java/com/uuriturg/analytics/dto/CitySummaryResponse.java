package com.uuriturg.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "City-wide rental market summary aggregated across all Tartu neighborhoods")
public class CitySummaryResponse {

    @Schema(description = "Total active listings across all neighborhoods", example = "142")
    private Integer totalListings;

    @Schema(description = "Lowest average neighborhood price in EUR", example = "350.00")
    private BigDecimal cheapestPrice;

    @Schema(description = "Highest average neighborhood price in EUR", example = "900.00")
    private BigDecimal mostExpensivePrice;

    @Schema(description = "City-wide average rent in EUR", example = "560.00")
    private BigDecimal averagePrice;

    @Schema(description = "City-wide average price per m² in EUR", example = "11.80")
    private BigDecimal averagePricePerSqm;

    @Schema(description = "Number of neighborhoods with available snapshot data", example = "8")
    private Integer neighborhoodsTracked;
}
