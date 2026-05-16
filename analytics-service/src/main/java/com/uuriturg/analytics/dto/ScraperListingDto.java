package com.uuriturg.analytics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScraperListingDto {

    private UUID listingId;
    private String title;
    private BigDecimal price;
    private BigDecimal size;
    private BigDecimal pricePerSqm;
    private Integer rooms;
    private String neighborhood;
    private String url;
    private Boolean isActive;
}
