package com.uuriturg.alert.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingEventDto {

    private UUID listingId;
    private String title;
    private BigDecimal price;
    private String neighborhood;
    private BigDecimal size;
    private Integer rooms;
    private String url;
    private LocalDateTime scrapedAt;
}
