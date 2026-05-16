package com.uuriturg.alert.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID matchId;

    @Column(nullable = false)
    private UUID alertId;

    @Column(nullable = false)
    private UUID listingId;

    private LocalDateTime matchedAt;

    private Boolean notified;

    @PrePersist
    void prePersist() {
        if (matchedAt == null) matchedAt = LocalDateTime.now();
        if (notified == null) notified = false;
    }
}
