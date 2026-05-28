package com.uuriturg.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "neighborhood_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeighborhoodSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID snapshotId;

    @Column(nullable = false)
    private String neighborhood;

    @Column(nullable = false)
    private LocalDate date;

    private BigDecimal averagePrice;

    private BigDecimal averagePricePerSqm;

    private BigDecimal medianPrice;

    private Integer listingCount;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal priceChangePercent;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (date == null) date = LocalDate.now();
        if (listingCount == null) listingCount = 0;
    }
}
