package com.uuriturg.scraper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_history", indexes = {
        @Index(name = "idx_ph_listing", columnList = "listingId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    void prePersist() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }
}
