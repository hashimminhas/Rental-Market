package com.uuriturg.scraper.repository;

import com.uuriturg.scraper.domain.PriceHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IPriceHistoryRepository extends CrudRepository<PriceHistory, UUID> {

    List<PriceHistory> findByListingIdOrderByRecordedAtAsc(UUID listingId);

    boolean existsByListingId(UUID listingId);
}
