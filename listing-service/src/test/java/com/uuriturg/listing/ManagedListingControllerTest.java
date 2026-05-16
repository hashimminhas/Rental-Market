package com.uuriturg.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.listing.controller.ManagedListingController;
import com.uuriturg.listing.domain.ManagedListingStatus;
import com.uuriturg.listing.dto.*;
import com.uuriturg.listing.exception.ListingAlreadyClaimedException;
import com.uuriturg.listing.exception.ManagedListingNotFoundException;
import com.uuriturg.listing.service.ManagedListingService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagedListingController.class)
class ManagedListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManagedListingService managedListingService;

    private final UUID managedId  = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID scrapedId  = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID landlordId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private ManagedListingResponse sampleResponse() {
        return ManagedListingResponse.builder()
                .managedListingId(managedId)
                .scrapedListingId(scrapedId)
                .landlordId(landlordId)
                .title("Cosy 2-room flat in Kesklinn")
                .description("Nice place")
                .price(new BigDecimal("550.00"))
                .size(new BigDecimal("52.0"))
                .rooms(2)
                .neighborhood("Kesklinn")
                .address("Raatuse 22")
                .originalUrl("https://kv.ee/123")
                .status(ManagedListingStatus.AVAILABLE)
                .claimedAt(LocalDateTime.now())
                .build();
    }

    private ManagedListingSummaryResponse sampleSummary() {
        return ManagedListingSummaryResponse.builder()
                .managedListingId(managedId)
                .landlordId(landlordId)
                .title("Cosy 2-room flat in Kesklinn")
                .neighborhood("Kesklinn")
                .price(new BigDecimal("550.00"))
                .rooms(2)
                .status(ManagedListingStatus.AVAILABLE)
                .claimedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllManagedListings_returns200() throws Exception {
        when(managedListingService.getAllManagedListings()).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/listings/managed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].neighborhood").value("Kesklinn"));
    }

    @Test
    void getAvailable_returns200() throws Exception {
        when(managedListingService.getAvailable(null)).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/listings/managed/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void getAvailable_withNeighborhood_returns200() throws Exception {
        when(managedListingService.getAvailable("Kesklinn")).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/listings/managed/available").param("neighborhood", "Kesklinn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].neighborhood").value("Kesklinn"));
    }

    @Test
    void getById_returns200() throws Exception {
        when(managedListingService.getById(managedId)).thenReturn(sampleResponse());

        mockMvc.perform(get("/listings/managed/{id}", managedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managedListingId").value(managedId.toString()))
                .andExpect(jsonPath("$.price").value(550.00));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(managedListingService.getById(managedId))
                .thenThrow(new ManagedListingNotFoundException(managedId));

        mockMvc.perform(get("/listings/managed/{id}", managedId))
                .andExpect(status().isNotFound());
    }

    @Test
    void claimListing_returns200() throws Exception {
        ClaimListingRequest req = ClaimListingRequest.builder()
                .landlordId(landlordId)
                .scrapedListingId(scrapedId)
                .build();

        when(managedListingService.claimListing(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/listings/managed/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managedListingId").value(managedId.toString()))
                .andExpect(jsonPath("$.title").value("Cosy 2-room flat in Kesklinn"));
    }

    @Test
    void claimListing_returns409_whenAlreadyClaimed() throws Exception {
        ClaimListingRequest req = ClaimListingRequest.builder()
                .landlordId(landlordId)
                .scrapedListingId(scrapedId)
                .build();

        when(managedListingService.claimListing(any()))
                .thenThrow(new ListingAlreadyClaimedException(scrapedId));

        mockMvc.perform(post("/listings/managed/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateListing_returns200() throws Exception {
        UpdateManagedListingRequest req = UpdateManagedListingRequest.builder()
                .price(new BigDecimal("575.00"))
                .status(ManagedListingStatus.RENTED)
                .build();

        ManagedListingResponse updated = sampleResponse();
        updated.setPrice(new BigDecimal("575.00"));
        updated.setStatus(ManagedListingStatus.RENTED);

        when(managedListingService.updateListing(eq(managedId), any())).thenReturn(updated);

        mockMvc.perform(put("/listings/managed/{id}", managedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RENTED"))
                .andExpect(jsonPath("$.price").value(575.00));
    }

    @Test
    void withdrawListing_returns200() throws Exception {
        ManagedListingResponse withdrawn = sampleResponse();
        withdrawn.setStatus(ManagedListingStatus.WITHDRAWN);

        when(managedListingService.withdrawListing(managedId)).thenReturn(withdrawn);

        mockMvc.perform(patch("/listings/managed/{id}/withdraw", managedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    void getByLandlord_returns200() throws Exception {
        when(managedListingService.getByLandlord(landlordId)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/listings/managed/landlord/{landlordId}", landlordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].landlordId").value(landlordId.toString()));
    }
}
