package com.uuriturg.scraper;

import com.uuriturg.scraper.controller.ListingController;
import com.uuriturg.scraper.dto.ListingResponse;
import com.uuriturg.scraper.exception.ListingNotFoundException;
import com.uuriturg.scraper.service.ListingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingService listingService;

    private static final UUID LISTING_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private ListingResponse buildSampleResponse() {
        return ListingResponse.builder()
                .listingId(LISTING_ID)
                .source("KV_EE")
                .externalId("98765")
                .title("2-room apartment in Kesklinn")
                .price(new BigDecimal("550.00"))
                .size(new BigDecimal("48.50"))
                .pricePerSqm(new BigDecimal("11.34"))
                .rooms(2)
                .neighborhood("Kesklinn")
                .street("Riia 10")
                .city("Tartu")
                .url("https://www.kv.ee/kinnisvara/98765")
                .scrapedAt(LocalDateTime.now())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Happy path tests ──────────────────────────────────────────────────────

    @Test
    void getListingById_returns200_withListingData_whenFound() throws Exception {
        when(listingService.findById(LISTING_ID)).thenReturn(buildSampleResponse());

        mockMvc.perform(get("/listings/{id}", LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId").value(LISTING_ID.toString()))
                .andExpect(jsonPath("$.source").value("KV_EE"))
                .andExpect(jsonPath("$.neighborhood").value("Kesklinn"))
                .andExpect(jsonPath("$.price").value(550.00))
                .andExpect(jsonPath("$.rooms").value(2));
    }

    @Test
    void getAllListings_returns200_withFilteredResults() throws Exception {
        when(listingService.findAll(any(), any(), isNull()))
                .thenReturn(List.of(buildSampleResponse()));

        mockMvc.perform(get("/listings")
                        .param("neighborhood", "Kesklinn")
                        .param("maxPrice", "600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].neighborhood").value("Kesklinn"));
    }

    @Test
    void getAllListings_returns200_emptyList_whenNoMatch() throws Exception {
        when(listingService.findAll(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/listings").param("neighborhood", "Annelinn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getLatestListings_returns200_withList() throws Exception {
        when(listingService.findLatest()).thenReturn(List.of(buildSampleResponse()));

        mockMvc.perform(get("/listings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("KV_EE"));
    }

    // ─── Error case tests ──────────────────────────────────────────────────────

    @Test
    void getListingById_returns404_whenListingNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(listingService.findById(unknownId))
                .thenThrow(new ListingNotFoundException(unknownId));

        mockMvc.perform(get("/listings/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }
}
