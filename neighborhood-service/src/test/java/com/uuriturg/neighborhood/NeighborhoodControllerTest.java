package com.uuriturg.neighborhood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.neighborhood.client.AnalyticsClient;
import com.uuriturg.neighborhood.controller.NeighborhoodController;
import com.uuriturg.neighborhood.dto.*;
import com.uuriturg.neighborhood.exception.DuplicateReviewException;
import com.uuriturg.neighborhood.exception.NeighborhoodNotFoundException;
import com.uuriturg.neighborhood.service.NeighborhoodService;
import com.uuriturg.neighborhood.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NeighborhoodController.class)
class NeighborhoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NeighborhoodService neighborhoodService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private AnalyticsClient analyticsClient;

    private final UUID neighborhoodId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId         = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private NeighborhoodSummaryResponse summary() {
        return NeighborhoodSummaryResponse.builder()
                .neighborhoodId(neighborhoodId)
                .name("Kesklinn")
                .slug("kesklinn")
                .distanceToCenter(0.3)
                .averageRating(4.5)
                .reviewCount(2)
                .build();
    }

    private NeighborhoodResponse fullProfile() {
        return NeighborhoodResponse.builder()
                .neighborhoodId(neighborhoodId)
                .name("Kesklinn")
                .slug("kesklinn")
                .description("City centre")
                .distanceToCenter(0.3)
                .characteristics("central,lively")
                .averageRating(4.5)
                .reviewCount(2)
                .averagePrice(new BigDecimal("520.00"))
                .averagePricePerSqm(new BigDecimal("9.50"))
                .listingCount(14)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllNeighborhoods_returns200() throws Exception {
        when(neighborhoodService.getAllNeighborhoods()).thenReturn(List.of(summary()));

        mockMvc.perform(get("/neighborhoods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].slug").value("kesklinn"));
    }

    @Test
    void getBySlug_returns200() throws Exception {
        when(neighborhoodService.getNeighborhoodBySlug("kesklinn")).thenReturn(fullProfile());

        mockMvc.perform(get("/neighborhoods/kesklinn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kesklinn"))
                .andExpect(jsonPath("$.averagePrice").value(520.00));
    }

    @Test
    void getBySlug_returns404_whenNotFound() throws Exception {
        when(neighborhoodService.getNeighborhoodBySlug("unknown"))
                .thenThrow(new NeighborhoodNotFoundException("unknown"));

        mockMvc.perform(get("/neighborhoods/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createNeighborhood_returns200() throws Exception {
        CreateNeighborhoodRequest req = CreateNeighborhoodRequest.builder()
                .name("Kesklinn")
                .description("City centre")
                .distanceToCenter(0.3)
                .characteristics("central,lively")
                .build();

        when(neighborhoodService.createNeighborhood(any())).thenReturn(fullProfile());

        mockMvc.perform(post("/neighborhoods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("kesklinn"));
    }

    @Test
    void addReview_returns200() throws Exception {
        ReviewRequest req = ReviewRequest.builder()
                .userId(userId)
                .neighborhoodId(neighborhoodId)
                .rating(4)
                .comment("Great area!")
                .build();

        ReviewResponse resp = ReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .neighborhoodId(neighborhoodId)
                .userId(userId)
                .rating(4)
                .comment("Great area!")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.addReview(any())).thenReturn(resp);

        mockMvc.perform(post("/neighborhoods/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Great area!"));
    }

    @Test
    void addReview_returns409_whenDuplicate() throws Exception {
        ReviewRequest req = ReviewRequest.builder()
                .userId(userId)
                .neighborhoodId(neighborhoodId)
                .rating(3)
                .build();

        when(reviewService.addReview(any())).thenThrow(new DuplicateReviewException());

        mockMvc.perform(post("/neighborhoods/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void getReviewsForNeighborhood_returns200() throws Exception {
        ReviewResponse rev = ReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .neighborhoodId(neighborhoodId)
                .userId(userId)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewsForNeighborhood(neighborhoodId)).thenReturn(List.of(rev));

        mockMvc.perform(get("/neighborhoods/{id}/reviews", neighborhoodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getReviewsByUser_returns200() throws Exception {
        ReviewResponse rev = ReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .neighborhoodId(neighborhoodId)
                .userId(userId)
                .rating(4)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewsByUser(userId)).thenReturn(List.of(rev));

        mockMvc.perform(get("/neighborhoods/reviews/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPriceData_returns200() throws Exception {
        when(neighborhoodService.getNeighborhoodBySlug("kesklinn")).thenReturn(fullProfile());
        when(analyticsClient.getPriceForNeighborhood("Kesklinn")).thenReturn(
                AnalyticsPriceDto.builder()
                        .neighborhood("Kesklinn")
                        .averagePrice(new BigDecimal("520.00"))
                        .averagePricePerSqm(new BigDecimal("9.50"))
                        .listingCount(14)
                        .build()
        );

        mockMvc.perform(get("/neighborhoods/kesklinn/price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averagePrice").value(520.00))
                .andExpect(jsonPath("$.listingCount").value(14));
    }
}
