package com.uuriturg.scraper.scraper;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import com.uuriturg.scraper.dto.ScrapeJobResponse;
import com.uuriturg.scraper.messaging.ListingEventPublisher;
import com.uuriturg.scraper.service.ListingService;
import com.uuriturg.scraper.service.ScrapeJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScraperOrchestrator {

    private final KvEeScraper kvEeScraper;
    private final City24Scraper city24Scraper;
    private final ListingService listingService;
    private final ScrapeJobService scrapeJobService;
    private final ListingEventPublisher eventPublisher;

    // auto-runs every 6 hours as configured in application.yml
    @Scheduled(cron = "${scraper.schedule.cron}")
    public void scheduledRun() {
        log.info("Scheduled scrape triggered");
        ScrapeJobResponse job = scrapeJobService.startJob(Source.KV_EE);
        runAll(job.getJobId());
    }

    // called by POST /scraper/trigger — runs in background thread so the HTTP response returns immediately
    public void triggerAsync(UUID jobId) {
        new Thread(() -> {
            log.info("Manual scrape triggered, jobId={}", jobId);
            runAll(jobId);
        }, "scraper-thread-" + jobId).start();
    }

    public void runAll(UUID jobId) {
        int totalFound = 0;
        int totalNew = 0;

        try {
            // --- KV.ee ---
            List<Listing> kvListings = kvEeScraper.scrape();
            totalFound += kvListings.size();
            for (Listing listing : kvListings) {
                boolean isNew = listingService.saveOrUpdate(listing);
                if (isNew) {
                    totalNew++;
                    // fetch the saved listing to get the generated UUID before publishing
                    try {
                        eventPublisher.publishListingNew(listing);
                    } catch (Exception e) {
                        log.warn("Failed to publish listing.new event for KV.ee listing {}: {}",
                                listing.getExternalId(), e.getMessage());
                    }
                }
            }

            // --- City24 ---
            List<Listing> city24Listings = city24Scraper.scrape();
            totalFound += city24Listings.size();
            for (Listing listing : city24Listings) {
                boolean isNew = listingService.saveOrUpdate(listing);
                if (isNew) {
                    totalNew++;
                    try {
                        eventPublisher.publishListingNew(listing);
                    } catch (Exception e) {
                        log.warn("Failed to publish listing.new event for City24 listing {}: {}",
                                listing.getExternalId(), e.getMessage());
                    }
                }
            }

            scrapeJobService.completeJob(jobId, totalFound, totalNew);
            log.info("Scrape complete — found={} new={}", totalFound, totalNew);

        } catch (Exception e) {
            log.error("Scrape run failed: {}", e.getMessage());
            scrapeJobService.failJob(jobId);
        }
    }
}
