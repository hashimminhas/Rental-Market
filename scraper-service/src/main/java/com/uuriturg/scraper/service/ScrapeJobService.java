package com.uuriturg.scraper.service;

import com.uuriturg.scraper.domain.Source;
import com.uuriturg.scraper.dto.ScrapeJobResponse;
import com.uuriturg.scraper.dto.ScrapeStatusResponse;

import java.util.List;
import java.util.UUID;

public interface ScrapeJobService {

    ScrapeJobResponse startJob(Source source);

    void completeJob(UUID jobId, int listingsFound, int newListings);

    void failJob(UUID jobId);

    ScrapeStatusResponse getStatus();

    List<ScrapeJobResponse> getRecentJobs();
}
