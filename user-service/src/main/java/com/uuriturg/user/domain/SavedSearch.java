package com.uuriturg.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saved_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID searchId;

    @Column(nullable = false)
    private UUID userId;

    private String neighborhood;

    private BigDecimal maxPrice;

    private BigDecimal minSize;

    private Integer minRooms;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
