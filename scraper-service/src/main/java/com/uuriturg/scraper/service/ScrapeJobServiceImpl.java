package com.uuriturg.scraper.service;

import com.uuriturg.scraper.domain.JobStatus;
import com.uuriturg.scraper.domain.ScrapeJob;
import com.uuriturg.scraper.domain.Source;
import com.uuriturg.scraper.dto.ScrapeJobResponse;
import com.uuriturg.scraper.dto.ScrapeStatusResponse;
import com.uuriturg.scraper.repository.IScrapeJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapeJobServiceImpl implements ScrapeJobService {

    private final IScrapeJobRepository scrapeJobRepository;
    private final ListingService listingService;

    @Override
    public ScrapeJobResponse startJob(Source source) {
        ScrapeJob job = ScrapeJob.builder()
                .source(source)
                .status(JobStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .listingsFound(0)
                .newListings(0)
                .build();
        ScrapeJob saved = scrapeJobRepository.save(job);
        return toResponse(saved);
    }

    @Override
    public void completeJob(UUID jobId, int listingsFound, int newListings) {
        scrapeJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setListingsFound(listingsFound);
            job.setNewListings(newListings);
            scrapeJobRepository.save(job);
        });
    }

    @Override
    public void failJob(UUID jobId) {
        scrapeJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            scrapeJobRepository.save(job);
        });
    }

    @Override
    public ScrapeStatusResponse getStatus() {
        Optional<ScrapeJob> runningJob = scrapeJobRepository
                .findFirstByStatusOrderByStartedAtDesc(JobStatus.RUNNING);

        Optional<ScrapeJob> lastCompleted = scrapeJobRepository
                .findFirstByOrderByCompletedAtDesc();

        LocalDateTime lastScrapeTime = lastCompleted
                .map(ScrapeJob::getCompletedAt)
                .orElse(null);

        String currentStatus = runningJob.isPresent() ? "RUNNING" : "IDLE";
        UUID currentJobId = runningJob.map(ScrapeJob::getJobId).orElse(null);

        return ScrapeStatusResponse.builder()
                .lastScrapeTime(lastScrapeTime)
                .totalActiveListings(listingService.countActive())
                .currentJobStatus(currentStatus)
                .currentJobId(currentJobId)
                .build();
    }

    @Override
    public List<ScrapeJobResponse> getRecentJobs() {
        return scrapeJobRepository.findTop20ByOrderByStartedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ScrapeJobResponse toResponse(ScrapeJob job) {
        return ScrapeJobResponse.builder()
                .jobId(job.getJobId())
                .source(job.getSource() != null ? job.getSource().name() : null)
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .listingsFound(job.getListingsFound())
                .newListings(job.getNewListings())
                .build();
    }
}
