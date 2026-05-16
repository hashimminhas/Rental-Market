package com.uuriturg.analytics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPoint {

    private LocalDate date;
    private BigDecimal averagePrice;
    private BigDecimal averagePricePerSqm;
    private Integer listingCount;
}
