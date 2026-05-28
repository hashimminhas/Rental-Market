package com.uuriturg.analytics.service;

import com.uuriturg.analytics.client.ScraperClient;
import com.uuriturg.analytics.domain.NeighborhoodSnapshot;
import com.uuriturg.analytics.dto.*;
import com.uuriturg.analytics.exception.SnapshotNotFoundException;
import com.uuriturg.analytics.repository.INeighborhoodSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final List<String> NEIGHBORHOODS = List.of(
            "Kesklinn", "Ülejõe", "Tammelinn", "Annelinn",
            "Karlova", "Veeriku", "Tähtvere", "Supilinn", "Ränilinn", "Maarjamõisa"
    );

    private final INeighborhoodSnapshotRepository snapshotRepository;
    private final ScraperClient scraperClient;

    @Override
    public List<NeighborhoodSummaryResponse> getLatestPerNeighborhood() {
        return NEIGHBORHOODS.stream()
                .map(name -> snapshotRepository.findFirstByNeighborhoodOrderByDateDesc(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TrendResponse getTrends(String neighborhood, int days) {
        if (days <= 0) days = 30;
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();

        List<NeighborhoodSnapshot> snapshots = snapshotRepository
                .findByNeighborhoodAndDateBetweenOrderByDateAsc(neighborhood, from, to);

        if (snapshots.isEmpty()) {
            throw new SnapshotNotFoundException(neighborhood);
        }

        List<TrendDataPoint> points = snapshots.stream()
                .map(s -> TrendDataPoint.builder()
                        .date(s.getDate())
                        .averagePrice(s.getAveragePrice())
                        .averagePricePerSqm(s.getAveragePricePerSqm())
                        .listingCount(s.getListingCount())
                        .build())
                .collect(Collectors.toList());

        return TrendResponse.builder()
                .neighborhood(neighborhood)
                .days(days)
                .dataPoints(points.size())
                .trend(points)
                .build();
    }

    @Override
    public CitySummaryResponse getCitySummary() {
        List<NeighborhoodSnapshot> latest = NEIGHBORHOODS.stream()
                .map(n -> snapshotRepository.findFirstByNeighborhoodOrderByDateDesc(n))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (latest.isEmpty()) {
            return CitySummaryResponse.builder()
                    .totalListings(0)
                    .neighborhoodsTracked(0)
                    .build();
        }

        int totalListings = latest.stream()
                .mapToInt(s -> s.getListingCount() != null ? s.getListingCount() : 0)
                .sum();

        BigDecimal cheapest = latest.stream()
                .map(NeighborhoodSnapshot::getMinPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);

        BigDecimal mostExpensive = latest.stream()
                .map(NeighborhoodSnapshot::getMaxPrice)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        BigDecimal avgPrice = average(latest.stream()
                .map(NeighborhoodSnapshot::getAveragePrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        BigDecimal avgPricePerSqm = average(latest.stream()
                .map(NeighborhoodSnapshot::getAveragePricePerSqm)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return CitySummaryResponse.builder()
                .totalListings(totalListings)
                .cheapestPrice(cheapest)
                .mostExpensivePrice(mostExpensive)
                .averagePrice(avgPrice)
                .averagePricePerSqm(avgPricePerSqm)
                .neighborhoodsTracked(latest.size())
                .build();
    }

    @Override
    public List<CheapestListingResponse> getCheapest(String neighborhood, BigDecimal maxPrice) {
        List<ScraperListingDto> listings = scraperClient.getListings(neighborhood, maxPrice);

        return listings.stream()
                .filter(l -> l.getPrice() != null)
                .sorted(Comparator.comparing(ScraperListingDto::getPrice))
                .limit(10)
                .map(l -> CheapestListingResponse.builder()
                        .listingId(l.getListingId())
                        .title(l.getTitle())
                        .price(l.getPrice())
                        .size(l.getSize())
                        .pricePerSqm(l.getPricePerSqm())
                        .rooms(l.getRooms())
                        .neighborhood(l.getNeighborhood())
                        .url(l.getUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> computeAndSave() {
        int snapshotsCreated = 0;

        for (String neighborhood : NEIGHBORHOODS) {
            try {
                List<ScraperListingDto> listings = scraperClient.getListingsByNeighborhood(neighborhood);
                if (listings.isEmpty()) {
                    log.info("No listings found for neighborhood: {}", neighborhood);
                    continue;
                }

                List<BigDecimal> prices = listings.stream()
                        .map(ScraperListingDto::getPrice)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                List<BigDecimal> pricesPerSqm = listings.stream()
                        .map(ScraperListingDto::getPricePerSqm)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                BigDecimal avgPrice = average(prices);
                BigDecimal avgPricePerSqm = average(pricesPerSqm);
                BigDecimal medianPrice = median(prices);
                BigDecimal minPrice = prices.stream().min(Comparator.naturalOrder()).orElse(null);
                BigDecimal maxPrice = prices.stream().max(Comparator.naturalOrder()).orElse(null);

                // compute price change vs previous snapshot
                BigDecimal priceChangePercent = null;
                Optional<NeighborhoodSnapshot> previous = snapshotRepository
                        .findFirstByNeighborhoodOrderByDateDesc(neighborhood);
                if (previous.isPresent() && previous.get().getAveragePrice() != null && avgPrice != null) {
                    BigDecimal prev = previous.get().getAveragePrice();
                    if (prev.compareTo(BigDecimal.ZERO) != 0) {
                        priceChangePercent = avgPrice.subtract(prev)
                                .divide(prev, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                NeighborhoodSnapshot snapshot = NeighborhoodSnapshot.builder()
                        .neighborhood(neighborhood)
                        .date(LocalDate.now())
                        .averagePrice(avgPrice)
                        .averagePricePerSqm(avgPricePerSqm)
                        .medianPrice(medianPrice)
                        .listingCount(listings.size())
                        .priceChangePercent(priceChangePercent)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .build();

                snapshotRepository.save(snapshot);
                snapshotsCreated++;
                log.info("Saved snapshot for {} — {} listings, avg price {}",
                        neighborhood, listings.size(), avgPrice);

            } catch (Exception e) {
                log.error("Failed to compute snapshot for {}: {}", neighborhood, e.getMessage());
            }
        }

        return Map.of(
                "snapshotsCreated", snapshotsCreated,
                "neighborhoods", NEIGHBORHOODS.size(),
                "date", LocalDate.now().toString()
        );
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return null;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return null;
        List<BigDecimal> sorted = values.stream().sorted().collect(Collectors.toList());
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return sorted.get(mid - 1).add(sorted.get(mid))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return sorted.get(mid);
    }

    private NeighborhoodSummaryResponse toSummaryResponse(NeighborhoodSnapshot s) {
        return NeighborhoodSummaryResponse.builder()
                .neighborhood(s.getNeighborhood())
                .snapshotDate(s.getDate())
                .averagePrice(s.getAveragePrice())
                .averagePricePerSqm(s.getAveragePricePerSqm())
                .medianPrice(s.getMedianPrice())
                .listingCount(s.getListingCount())
                .priceChangePercent(s.getPriceChangePercent())
                .build();
    }
}
