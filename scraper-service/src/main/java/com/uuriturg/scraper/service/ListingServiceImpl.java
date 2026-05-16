package com.uuriturg.scraper.service;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.dto.ListingResponse;
import com.uuriturg.scraper.exception.ListingNotFoundException;
import com.uuriturg.scraper.repository.IListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final IListingRepository listingRepository;

    @Override
    public List<ListingResponse> findAll(String neighborhood, BigDecimal maxPrice, BigDecimal minSize) {
        List<Listing> listings = listingRepository.findByIsActiveTrue();

        return listings.stream()
                .filter(l -> neighborhood == null || neighborhood.isBlank() ||
                        (l.getNeighborhood() != null && l.getNeighborhood().equalsIgnoreCase(neighborhood)))
                .filter(l -> maxPrice == null ||
                        (l.getPrice() != null && l.getPrice().compareTo(maxPrice) <= 0))
                .filter(l -> minSize == null ||
                        (l.getSize() != null && l.getSize().compareTo(minSize) >= 0))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ListingResponse> findLatest() {
        return listingRepository.findTop50ByOrderByScrapedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ListingResponse findById(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        return toResponse(listing);
    }

    @Override
    public boolean saveOrUpdate(Listing incoming) {
        Optional<Listing> existing = listingRepository.findBySourceAndExternalId(
                incoming.getSource(), incoming.getExternalId());

        if (existing.isPresent()) {
            Listing toUpdate = existing.get();
            toUpdate.setPrice(incoming.getPrice());
            toUpdate.setIsActive(true);
            toUpdate.setScrapedAt(LocalDateTime.now());
            toUpdate.setTitle(incoming.getTitle());
            toUpdate.setSize(incoming.getSize());
            toUpdate.setRooms(incoming.getRooms());
            listingRepository.save(toUpdate);
            return false;
        }

        incoming.setScrapedAt(LocalDateTime.now());
        listingRepository.save(incoming);
        return true;
    }

    @Override
    public long countActive() {
        return StreamSupport
                .stream(listingRepository.findAll().spliterator(), false)
                .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                .count();
    }

    private ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .listingId(listing.getListingId())
                .source(listing.getSource() != null ? listing.getSource().name() : null)
                .externalId(listing.getExternalId())
                .title(listing.getTitle())
                .price(listing.getPrice())
                .size(listing.getSize())
                .pricePerSqm(listing.getPricePerSqm())
                .rooms(listing.getRooms())
                .neighborhood(listing.getNeighborhood())
                .street(listing.getStreet())
                .city(listing.getCity())
                .postalCode(listing.getPostalCode())
                .url(listing.getUrl())
                .scrapedAt(listing.getScrapedAt())
                .isActive(listing.getIsActive())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}
