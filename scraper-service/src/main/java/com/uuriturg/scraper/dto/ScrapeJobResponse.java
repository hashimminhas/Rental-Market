package com.uuriturg.scraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A scrape job record tracking one scrape run")
public class ScrapeJobResponse {

    @Schema(description = "Unique job identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID jobId;

    @Schema(description = "Source portal that was scraped", example = "KV_EE")
    private String source;

    @Schema(description = "Timestamp when the scrape job started")
    private LocalDateTime startedAt;

    @Schema(description = "Timestamp when the scrape job finished (null if still running)")
    private LocalDateTime completedAt;

    @Schema(description = "Job status", example = "COMPLETED", allowableValues = {"RUNNING", "COMPLETED", "FAILED"})
    private String status;

    @Schema(description = "Total number of listings found on the source portal", example = "35")
    private Integer listingsFound;

    @Schema(description = "Number of listings that were new and saved to the database", example = "8")
    private Integer newListings;
}
