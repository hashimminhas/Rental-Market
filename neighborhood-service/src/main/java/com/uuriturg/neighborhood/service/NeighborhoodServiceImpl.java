package com.uuriturg.neighborhood.service;

import com.uuriturg.neighborhood.client.AnalyticsClient;
import com.uuriturg.neighborhood.domain.Neighborhood;
import com.uuriturg.neighborhood.dto.*;
import com.uuriturg.neighborhood.exception.NeighborhoodNotFoundException;
import com.uuriturg.neighborhood.repository.INeighborhoodRepository;
import com.uuriturg.neighborhood.repository.IReviewRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeighborhoodServiceImpl implements NeighborhoodService {

    private final INeighborhoodRepository neighborhoodRepository;
    private final IReviewRepository reviewRepository;
    private final AnalyticsClient analyticsClient;

    private static final List<Object[]> DEFAULTS = List.of(
            new Object[]{"Kesklinn",   "The vibrant city centre — home to the Town Hall Square, University of Tartu main building, and most restaurants and shops.", 0.3, "central,lively,walkable,historic"},
            new Object[]{"Ülejõe",     "A quiet residential area across the Emajõgi river, popular with students and young professionals.", 1.2, "quiet,riverside,student-friendly"},
            new Object[]{"Tammelinn",  "A leafy suburban district with spacious homes and good schools, favoured by families.", 2.4, "family-friendly,green,suburban"},
            new Object[]{"Annelinn",   "The largest residential district, built in the Soviet era — affordable rents and good public transport links.", 3.2, "affordable,well-connected,large"},
            new Object[]{"Karlova",    "A charming wooden-house neighbourhood with a bohemian character and many cafes and art spaces.", 1.5, "bohemian,historic,cosy,cafes"},
            new Object[]{"Veeriku",    "A quiet southern residential area with parks and green spaces, popular with families.", 2.8, "quiet,parks,family-friendly"},
            new Object[]{"Tähtvere",   "An upscale area near the Tähtvere Dendrological Park, featuring larger houses and a calm atmosphere.", 2.1, "upscale,green,calm,spacious"},
            new Object[]{"Supilinn",   "A charming historic quarter known for its colourful wooden houses and artistic community.", 1.0, "historic,artistic,unique,wooden-houses"},
            new Object[]{"Ränilinn",   "A modern residential suburb in the south-east with new apartment buildings and shopping centres nearby.", 4.0, "modern,suburban,new-builds"},
            new Object[]{"Maarjamõisa","Home to Tartu University Hospital and the science park — a quieter area popular with medical staff.", 2.6, "hospital,academic,quiet"}
    );

    @PostConstruct
    void autoSeed() {
        long count = StreamSupport.stream(neighborhoodRepository.findAll().spliterator(), false).count();
        if (count == 0) {
            int seeded = seedDefaultNeighborhoods();
            log.info("Auto-seeded {} default Tartu neighborhoods", seeded);
        }
    }

    @Override
    public int seedDefaultNeighborhoods() {
        int count = 0;
        for (Object[] row : DEFAULTS) {
            String name = (String) row[0];
            if (neighborhoodRepository.findByNameIgnoreCase(name).isEmpty()) {
                neighborhoodRepository.save(Neighborhood.builder()
                        .name(name)
                        .description((String) row[1])
                        .distanceToCenter((Double) row[2])
                        .characteristics((String) row[3])
                        .build());
                count++;
            }
        }
        return count;
    }

    @Override
    public List<NeighborhoodSummaryResponse> getAllNeighborhoods() {
        return StreamSupport.stream(neighborhoodRepository.findAll().spliterator(), false)
                .map(n -> {
                    List<com.uuriturg.neighborhood.domain.Review> reviews =
                            reviewRepository.findByNeighborhoodId(n.getNeighborhoodId());
                    double avg = reviews.stream()
                            .mapToInt(r -> r.getRating())
                            .average()
                            .orElse(0.0);
                    return NeighborhoodSummaryResponse.builder()
                            .neighborhoodId(n.getNeighborhoodId())
                            .name(n.getName())
                            .slug(n.getSlug())
                            .distanceToCenter(n.getDistanceToCenter())
                            .averageRating(Math.round(avg * 10.0) / 10.0)
                            .reviewCount(reviews.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public NeighborhoodResponse getNeighborhoodBySlug(String slug) {
        Neighborhood n = neighborhoodRepository.findBySlug(slug)
                .orElseThrow(() -> new NeighborhoodNotFoundException(slug));

        List<com.uuriturg.neighborhood.domain.Review> reviews =
                reviewRepository.findByNeighborhoodId(n.getNeighborhoodId());
        double avg = reviews.stream().mapToInt(r -> r.getRating()).average().orElse(0.0);

        AnalyticsPriceDto price = analyticsClient.getPriceForNeighborhood(n.getName());

        return NeighborhoodResponse.builder()
                .neighborhoodId(n.getNeighborhoodId())
                .name(n.getName())
                .slug(n.getSlug())
                .description(n.getDescription())
                .distanceToCenter(n.getDistanceToCenter())
                .characteristics(n.getCharacteristics())
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .reviewCount(reviews.size())
                .averagePrice(price != null ? price.getAveragePrice() : null)
                .averagePricePerSqm(price != null ? price.getAveragePricePerSqm() : null)
                .listingCount(price != null ? price.getListingCount() : null)
                .createdAt(n.getCreatedAt())
                .build();
    }

    @Override
    public NeighborhoodResponse createNeighborhood(CreateNeighborhoodRequest request) {
        Neighborhood saved = neighborhoodRepository.save(Neighborhood.builder()
                .name(request.getName())
                .description(request.getDescription())
                .distanceToCenter(request.getDistanceToCenter())
                .characteristics(request.getCharacteristics())
                .build());
        return getNeighborhoodBySlug(saved.getSlug());
    }
}
