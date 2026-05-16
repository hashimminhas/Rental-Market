package com.uuriturg.scraper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source", "externalId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID listingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(nullable = false)
    private String externalId;

    private String title;

    private BigDecimal price;

    private BigDecimal size;

    private BigDecimal pricePerSqm;

    private Integer rooms;

    private String neighborhood;

    private String street;

    private String city;

    private String postalCode;

    @Column(length = 1000)
    private String url;

    private LocalDateTime scrapedAt;

    private Boolean isActive;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (scrapedAt == null) scrapedAt = LocalDateTime.now();
        computePricePerSqm();
    }

    @PreUpdate
    void preUpdate() {
        computePricePerSqm();
    }

    private void computePricePerSqm() {
        if (price != null && size != null && size.compareTo(BigDecimal.ZERO) != 0) {
            pricePerSqm = price.divide(size, 2, RoundingMode.HALF_UP);
        }
    }
}
