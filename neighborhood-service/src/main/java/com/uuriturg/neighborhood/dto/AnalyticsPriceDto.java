package com.uuriturg.neighborhood.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPriceDto {

    private String neighborhood;
    private BigDecimal averagePrice;
    private BigDecimal averagePricePerSqm;
    private Integer listingCount;
}
