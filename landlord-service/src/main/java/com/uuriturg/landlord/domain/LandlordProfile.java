package com.uuriturg.landlord.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "landlord_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID landlordId;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String phoneNumber;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isVerified == null) isVerified = false;
        if (averageRating == null) averageRating = 0.0;
        if (reviewCount == null) reviewCount = 0;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
