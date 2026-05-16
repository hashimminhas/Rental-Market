package com.uuriturg.listing.service;

import com.uuriturg.listing.client.LandlordClient;
import com.uuriturg.listing.client.ScraperClient;
import com.uuriturg.listing.domain.ManagedListing;
import com.uuriturg.listing.domain.ManagedListingStatus;
import com.uuriturg.listing.dto.*;
import com.uuriturg.listing.exception.ListingAlreadyClaimedException;
import com.uuriturg.listing.exception.ManagedListingNotFoundException;
import com.uuriturg.listing.messaging.ListingClaimedEventPublisher;
import com.uuriturg.listing.repository.IManagedListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagedListingServiceImpl implements ManagedListingService {

    private final IManagedListingRepository managedListingRepository;
    private final ScraperClient scraperClient;
    private final LandlordClient landlordClient;
    private final ListingClaimedEventPublisher eventPublisher;

    @Override
    public ManagedListingResponse claimListing(ClaimListingRequest request) {
        if (managedListingRepository.existsByScrapedListingId(request.getScrapedListingId())) {
            throw new ListingAlreadyClaimedException(request.getScrapedListingId());
        }

        LandlordValidateDto landlord = landlordClient.getLandlordById(request.getLandlordId());
        if (landlord == null) {
            throw new IllegalArgumentException("Landlord not found: " + request.getLandlordId());
        }

        ScraperListingDto scraped = scraperClient.getListingById(request.getScrapedListingId());

        ManagedListing listing = ManagedListing.builder()
                .scrapedListingId(request.getScrapedListingId())
                .landlordId(request.getLandlordId())
                .title(request.getTitle() != null ? request.getTitle()
                        : (scraped != null ? scraped.getTitle() : "Untitled listing"))
                .description(request.getDescription())
                .price(request.getPrice() != null ? request.getPrice()
                        : (scraped != null ? scraped.getPrice() : null))
                .size(scraped != null ? scraped.getSize() : null)
                .rooms(scraped != null ? scraped.getRooms() : null)
                .neighborhood(scraped != null ? scraped.getNeighborhood() : null)
                .address(request.getAddress() != null ? request.getAddress()
                        : (scraped != null ? scraped.getStreet() : null))
                .originalUrl(scraped != null ? scraped.getUrl() : null)
                .build();

        ManagedListing saved = managedListingRepository.save(listing);
        log.info("Landlord {} claimed listing {} → managedListing {}", request.getLandlordId(), request.getScrapedListingId(), saved.getManagedListingId());

        eventPublisher.publishListingClaimed(saved, landlord.getDisplayName());
        return toResponse(saved);
    }

    @Override
    public List<ManagedListingSummaryResponse> getAllManagedListings() {
        return StreamSupport.stream(managedListingRepository.findAll().spliterator(), false)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public ManagedListingResponse getById(UUID managedListingId) {
        return toResponse(managedListingRepository.findById(managedListingId)
                .orElseThrow(() -> new ManagedListingNotFoundException(managedListingId)));
    }

    @Override
    public ManagedListingResponse getByScrapedListingId(UUID scrapedListingId) {
        return toResponse(managedListingRepository.findByScrapedListingId(scrapedListingId)
                .orElseThrow(() -> new ManagedListingNotFoundException(scrapedListingId)));
    }

    @Override
    public List<ManagedListingResponse> getByLandlord(UUID landlordId) {
        return managedListingRepository.findByLandlordId(landlordId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ManagedListingSummaryResponse> getAvailable(String neighborhood) {
        List<ManagedListing> results = (neighborhood != null && !neighborhood.isBlank())
                ? managedListingRepository.findByNeighborhoodIgnoreCaseAndStatus(neighborhood, ManagedListingStatus.AVAILABLE)
                : managedListingRepository.findByStatus(ManagedListingStatus.AVAILABLE);
        return results.stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public ManagedListingResponse updateListing(UUID managedListingId, UpdateManagedListingRequest request) {
        ManagedListing listing = managedListingRepository.findById(managedListingId)
                .orElseThrow(() -> new ManagedListingNotFoundException(managedListingId));
        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getPrice() != null) listing.setPrice(request.getPrice());
        if (request.getSize() != null) listing.setSize(request.getSize());
        if (request.getRooms() != null) listing.setRooms(request.getRooms());
        if (request.getAddress() != null) listing.setAddress(request.getAddress());
        if (request.getStatus() != null) listing.setStatus(request.getStatus());
        return toResponse(managedListingRepository.save(listing));
    }

    @Override
    public ManagedListingResponse withdrawListing(UUID managedListingId) {
        ManagedListing listing = managedListingRepository.findById(managedListingId)
                .orElseThrow(() -> new ManagedListingNotFoundException(managedListingId));
        listing.setStatus(ManagedListingStatus.WITHDRAWN);
        return toResponse(managedListingRepository.save(listing));
    }

    private ManagedListingSummaryResponse toSummary(ManagedListing l) {
        return ManagedListingSummaryResponse.builder()
                .managedListingId(l.getManagedListingId())
                .landlordId(l.getLandlordId())
                .title(l.getTitle())
                .neighborhood(l.getNeighborhood())
                .price(l.getPrice())
                .rooms(l.getRooms())
                .status(l.getStatus())
                .claimedAt(l.getClaimedAt())
                .build();
    }

    private ManagedListingResponse toResponse(ManagedListing l) {
        return ManagedListingResponse.builder()
                .managedListingId(l.getManagedListingId())
                .scrapedListingId(l.getScrapedListingId())
                .landlordId(l.getLandlordId())
                .title(l.getTitle())
                .description(l.getDescription())
                .price(l.getPrice())
                .size(l.getSize())
                .rooms(l.getRooms())
                .neighborhood(l.getNeighborhood())
                .address(l.getAddress())
                .originalUrl(l.getOriginalUrl())
                .status(l.getStatus())
                .claimedAt(l.getClaimedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
