package com.uuriturg.scraper.repository;

import com.uuriturg.scraper.domain.JobStatus;
import com.uuriturg.scraper.domain.ScrapeJob;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IScrapeJobRepository extends CrudRepository<ScrapeJob, UUID> {

    List<ScrapeJob> findTop20ByOrderByStartedAtDesc();

    Optional<ScrapeJob> findFirstByStatusOrderByStartedAtDesc(JobStatus status);

    Optional<ScrapeJob> findFirstByOrderByCompletedAtDesc();
}
