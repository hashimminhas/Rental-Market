package com.uuriturg.scraper.repository;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.Source;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IListingRepository extends CrudRepository<Listing, UUID> {

    List<Listing> findByIsActiveTrue();

    Optional<Listing> findBySourceAndExternalId(Source source, String externalId);

    List<Listing> findTop50ByOrderByScrapedAtDesc();

    List<Listing> findByNeighborhoodIgnoreCaseAndIsActiveTrue(String neighborhood);

    List<Listing> findByIsActiveTrueAndPriceLessThanEqual(java.math.BigDecimal maxPrice);

    List<Listing> findByIsActiveTrueAndSizeGreaterThanEqual(java.math.BigDecimal minSize);
}
