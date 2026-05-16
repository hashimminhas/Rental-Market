package com.uuriturg.listing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "managed_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID managedListingId;

    @Column(nullable = false, unique = true)
    private UUID scrapedListingId;

    @Column(nullable = false)
    private UUID landlordId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;
    private BigDecimal size;
    private Integer rooms;
    private String neighborhood;
    private String address;
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ManagedListingStatus status = ManagedListingStatus.AVAILABLE;

    private LocalDateTime claimedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (claimedAt == null) claimedAt = LocalDateTime.now();
        if (status == null) status = ManagedListingStatus.AVAILABLE;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
