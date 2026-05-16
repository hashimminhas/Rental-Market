package com.uuriturg.listing.repository;

import com.uuriturg.listing.domain.ManagedListing;
import com.uuriturg.listing.domain.ManagedListingStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IManagedListingRepository extends CrudRepository<ManagedListing, UUID> {

    boolean existsByScrapedListingId(UUID scrapedListingId);

    Optional<ManagedListing> findByScrapedListingId(UUID scrapedListingId);

    List<ManagedListing> findByLandlordId(UUID landlordId);

    List<ManagedListing> findByStatus(ManagedListingStatus status);

    List<ManagedListing> findByNeighborhoodIgnoreCaseAndStatus(String neighborhood, ManagedListingStatus status);
}
