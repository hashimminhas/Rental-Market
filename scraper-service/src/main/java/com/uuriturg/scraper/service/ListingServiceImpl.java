package com.uuriturg.scraper.service;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.domain.PriceHistory;
import com.uuriturg.scraper.dto.ListingResponse;
import com.uuriturg.scraper.dto.PriceHistoryResponse;
import com.uuriturg.scraper.exception.ListingNotFoundException;
import com.uuriturg.scraper.repository.IListingRepository;
import com.uuriturg.scraper.repository.IPriceHistoryRepository;
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
    private final IPriceHistoryRepository priceHistoryRepository;

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
            boolean priceChanged = incoming.getPrice() != null &&
                    (toUpdate.getPrice() == null || toUpdate.getPrice().compareTo(incoming.getPrice()) != 0);
            toUpdate.setPrice(incoming.getPrice());
            toUpdate.setIsActive(true);
            toUpdate.setScrapedAt(LocalDateTime.now());
            toUpdate.setTitle(incoming.getTitle());
            toUpdate.setSize(incoming.getSize());
            toUpdate.setRooms(incoming.getRooms());
            toUpdate.setUrl(incoming.getUrl());
            toUpdate.setImageUrl(incoming.getImageUrl());
            toUpdate.setNeighborhood(incoming.getNeighborhood());
            toUpdate.setStreet(incoming.getStreet());
            toUpdate.setCity(incoming.getCity());
            toUpdate.setSynthetic(incoming.getSynthetic());
            if (incoming.getLatitude() != null)  toUpdate.setLatitude(incoming.getLatitude());
            if (incoming.getLongitude() != null) toUpdate.setLongitude(incoming.getLongitude());
            listingRepository.save(toUpdate);
            if (priceChanged) recordPrice(toUpdate.getListingId(), incoming.getPrice());
            return false;
        }

        incoming.setScrapedAt(LocalDateTime.now());
        listingRepository.save(incoming);
        if (incoming.getPrice() != null) recordPrice(incoming.getListingId(), incoming.getPrice());
        return true;
    }

    @Override
    public long countActive() {
        return StreamSupport
                .stream(listingRepository.findAll().spliterator(), false)
                .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                .count();
    }

    @Override
    public List<PriceHistoryResponse> getPriceHistory(UUID listingId) {
        return priceHistoryRepository.findByListingIdOrderByRecordedAtAsc(listingId)
                .stream()
                .map(ph -> PriceHistoryResponse.builder()
                        .price(ph.getPrice())
                        .recordedAt(ph.getRecordedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void recordPrice(UUID listingId, BigDecimal price) {
        priceHistoryRepository.save(PriceHistory.builder()
                .listingId(listingId)
                .price(price)
                .recordedAt(LocalDateTime.now())
                .build());
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
                .imageUrl(listing.getImageUrl())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .scrapedAt(listing.getScrapedAt())
                .isActive(listing.getIsActive())
                .createdAt(listing.getCreatedAt())
                .synthetic(Boolean.TRUE.equals(listing.getSynthetic()))
                .build();
    }
}
