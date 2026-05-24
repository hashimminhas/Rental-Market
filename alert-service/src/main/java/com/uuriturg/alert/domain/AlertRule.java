package com.uuriturg.alert.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID alertId;

    @Column(nullable = false)
    private String email;

    private String name;

    private String neighborhood;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal minSize;

    private Integer minRooms;

    private Boolean isActive;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }
}
