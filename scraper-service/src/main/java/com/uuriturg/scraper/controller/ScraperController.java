package com.uuriturg.scraper.controller;

import com.uuriturg.scraper.domain.Source;
import com.uuriturg.scraper.dto.ScrapeJobResponse;
import com.uuriturg.scraper.dto.ScrapeStatusResponse;
import com.uuriturg.scraper.scraper.ScraperOrchestrator;
import com.uuriturg.scraper.service.ScrapeJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scraper")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Scraper", description = "Trigger scrape jobs and monitor their progress")
public class ScraperController {

    private final ScrapeJobService scrapeJobService;
    private final ScraperOrchestrator scraperOrchestrator;

    @Operation(
            summary = "Trigger a manual scrape run",
            description = "Starts an asynchronous scrape across KV.ee and City24. Returns 202 Accepted immediately with the job ID. Use GET /scraper/status or GET /scraper/jobs to track progress. The scraper also runs automatically every 6 hours."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Scrape job accepted and running in background"),
            @ApiResponse(responseCode = "500", description = "Failed to start the scrape job")
    })
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerScrape() {
        ScrapeJobResponse job = scrapeJobService.startJob(Source.KV_EE);
        scraperOrchestrator.triggerAsync(job.getJobId());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Scrape job started",
                "jobId", job.getJobId().toString(),
                "status", job.getStatus()
        ));
    }

    @Operation(
            summary = "Get current scraper status",
            description = "Returns the time of the last completed scrape, the total number of active listings in the database, and whether a scrape job is currently running."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status returned successfully")
    })
    @GetMapping("/status")
    public ResponseEntity<ScrapeStatusResponse> getStatus() {
        return ResponseEntity.ok(scrapeJobService.getStatus());
    }

    @Operation(
            summary = "List recent scrape jobs",
            description = "Returns the 20 most recent scrape job records ordered by start time descending. Each record shows source, duration, listings found, and new listings added."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job list returned successfully")
    })
    @GetMapping("/jobs")
    public ResponseEntity<List<ScrapeJobResponse>> getRecentJobs() {
        return ResponseEntity.ok(scrapeJobService.getRecentJobs());
    }
}
