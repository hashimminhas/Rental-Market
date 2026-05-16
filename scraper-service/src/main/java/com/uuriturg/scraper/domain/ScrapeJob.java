package com.uuriturg.scraper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scrape_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeJob {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    private Source source;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private Integer listingsFound;

    private Integer newListings;

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = LocalDateTime.now();
        if (listingsFound == null) listingsFound = 0;
        if (newListings == null) newListings = 0;
        if (status == null) status = JobStatus.RUNNING;
    }
}
