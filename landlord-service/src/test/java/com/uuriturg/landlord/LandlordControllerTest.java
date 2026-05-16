package com.uuriturg.landlord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.landlord.controller.LandlordController;
import com.uuriturg.landlord.dto.*;
import com.uuriturg.landlord.exception.DuplicateReviewException;
import com.uuriturg.landlord.exception.LandlordNotFoundException;
import com.uuriturg.landlord.service.LandlordService;
import com.uuriturg.landlord.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LandlordController.class)
class LandlordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LandlordService landlordService;

    @MockitoBean
    private ReviewService reviewService;

    private final UUID landlordId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId     = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID reviewerId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private LandlordResponse sampleLandlord() {
        return LandlordResponse.builder()
                .landlordId(landlordId)
                .userId(userId)
                .displayName("Jaan Tamm")
                .bio("Experienced landlord")
                .phoneNumber("+372 5555 1234")
                .isVerified(false)
                .averageRating(0.0)
                .reviewCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private LandlordSummaryResponse sampleSummary() {
        return LandlordSummaryResponse.builder()
                .landlordId(landlordId)
                .displayName("Jaan Tamm")
                .isVerified(false)
                .averageRating(4.0)
                .reviewCount(3)
                .build();
    }

    @Test
    void getAllLandlords_returns200() throws Exception {
        when(landlordService.getAllLandlords()).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/landlords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].displayName").value("Jaan Tamm"));
    }

    @Test
    void getTopRated_returns200() throws Exception {
        when(landlordService.getTopRated()).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/landlords/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].averageRating").value(4.0));
    }

    @Test
    void getById_returns200() throws Exception {
        when(landlordService.getById(landlordId)).thenReturn(sampleLandlord());

        mockMvc.perform(get("/landlords/{id}", landlordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.landlordId").value(landlordId.toString()))
                .andExpect(jsonPath("$.displayName").value("Jaan Tamm"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(landlordService.getById(landlordId)).thenThrow(new LandlordNotFoundException(landlordId));

        mockMvc.perform(get("/landlords/{id}", landlordId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLandlord_returns200() throws Exception {
        CreateLandlordRequest req = CreateLandlordRequest.builder()
                .userId(userId)
                .displayName("Jaan Tamm")
                .bio("Experienced landlord")
                .build();

        when(landlordService.createLandlord(any())).thenReturn(sampleLandlord());

        mockMvc.perform(post("/landlords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.landlordId").value(landlordId.toString()));
    }

    @Test
    void createLandlord_returns400_whenUserInvalid() throws Exception {
        CreateLandlordRequest req = CreateLandlordRequest.builder()
                .userId(userId)
                .displayName("Jaan Tamm")
                .build();

        when(landlordService.createLandlord(any()))
                .thenThrow(new IllegalArgumentException("User not found or inactive: " + userId));

        mockMvc.perform(post("/landlords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLandlord_returns200() throws Exception {
        UpdateLandlordRequest req = UpdateLandlordRequest.builder()
                .displayName("Jaan Tamm Updated")
                .build();

        LandlordResponse updated = sampleLandlord();
        updated.setDisplayName("Jaan Tamm Updated");
        when(landlordService.updateLandlord(eq(landlordId), any())).thenReturn(updated);

        mockMvc.perform(put("/landlords/{id}", landlordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Jaan Tamm Updated"));
    }

    @Test
    void verifyLandlord_returns200() throws Exception {
        LandlordResponse verified = sampleLandlord();
        verified.setIsVerified(true);
        when(landlordService.verifyLandlord(landlordId)).thenReturn(verified);

        mockMvc.perform(patch("/landlords/{id}/verify", landlordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerified").value(true));
    }

    @Test
    void addReview_returns200() throws Exception {
        TenantReviewRequest req = TenantReviewRequest.builder()
                .reviewerUserId(reviewerId)
                .landlordId(landlordId)
                .rating(4)
                .comment("Very professional.")
                .build();

        TenantReviewResponse resp = TenantReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .landlordId(landlordId)
                .reviewerUserId(reviewerId)
                .rating(4)
                .comment("Very professional.")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.addReview(any())).thenReturn(resp);

        mockMvc.perform(post("/landlords/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Very professional."));
    }

    @Test
    void addReview_returns409_whenDuplicate() throws Exception {
        TenantReviewRequest req = TenantReviewRequest.builder()
                .reviewerUserId(reviewerId)
                .landlordId(landlordId)
                .rating(3)
                .build();

        when(reviewService.addReview(any())).thenThrow(new DuplicateReviewException());

        mockMvc.perform(post("/landlords/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void getReviewsForLandlord_returns200() throws Exception {
        TenantReviewResponse rev = TenantReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .landlordId(landlordId)
                .reviewerUserId(reviewerId)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewsForLandlord(landlordId)).thenReturn(List.of(rev));

        mockMvc.perform(get("/landlords/{id}/reviews", landlordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));
    }
}
