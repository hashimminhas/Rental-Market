package com.uuriturg.scraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current status of the scraper service")
public class ScrapeStatusResponse {

    @Schema(description = "Timestamp of the last completed scrape run (null if never run)")
    private LocalDateTime lastScrapeTime;

    @Schema(description = "Total number of active listings currently in the database", example = "142")
    private Long totalActiveListings;

    @Schema(description = "Whether a scrape is currently in progress", example = "IDLE", allowableValues = {"RUNNING", "IDLE"})
    private String currentJobStatus;

    @Schema(description = "ID of the currently running job (null if idle)")
    private UUID currentJobId;
}
