package com.uuriturg.analytics;

import com.uuriturg.analytics.controller.AnalyticsController;
import com.uuriturg.analytics.dto.*;
import com.uuriturg.analytics.exception.SnapshotNotFoundException;
import com.uuriturg.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    // ─── Happy path tests ──────────────────────────────────────────────────────

    @Test
    void getNeighborhoods_returns200_withSnapshotList() throws Exception {
        NeighborhoodSummaryResponse summary = NeighborhoodSummaryResponse.builder()
                .neighborhood("Kesklinn")
                .snapshotDate(LocalDate.now())
                .averagePrice(new BigDecimal("580.00"))
                .averagePricePerSqm(new BigDecimal("12.50"))
                .listingCount(14)
                .priceChangePercent(new BigDecimal("2.30"))
                .build();

        when(analyticsService.getLatestPerNeighborhood()).thenReturn(List.of(summary));

        mockMvc.perform(get("/analytics/neighborhoods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].neighborhood").value("Kesklinn"))
                .andExpect(jsonPath("$[0].averagePrice").value(580.00))
                .andExpect(jsonPath("$[0].listingCount").value(14));
    }

    @Test
    void getTrends_returns200_withTrendData() throws Exception {
        TrendResponse trend = TrendResponse.builder()
                .neighborhood("Tammelinn")
                .days(30)
                .dataPoints(3)
                .trend(List.of(
                        TrendDataPoint.builder().date(LocalDate.now().minusDays(2))
                                .averagePrice(new BigDecimal("520.00")).listingCount(8).build(),
                        TrendDataPoint.builder().date(LocalDate.now().minusDays(1))
                                .averagePrice(new BigDecimal("530.00")).listingCount(9).build(),
                        TrendDataPoint.builder().date(LocalDate.now())
                                .averagePrice(new BigDecimal("525.00")).listingCount(10).build()
                ))
                .build();

        when(analyticsService.getTrends(eq("Tammelinn"), eq(30))).thenReturn(trend);

        mockMvc.perform(get("/analytics/trends")
                        .param("neighborhood", "Tammelinn")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood").value("Tammelinn"))
                .andExpect(jsonPath("$.dataPoints").value(3))
                .andExpect(jsonPath("$.trend.length()").value(3));
    }

    @Test
    void getSummary_returns200_withCitySummary() throws Exception {
        CitySummaryResponse summary = CitySummaryResponse.builder()
                .totalListings(142)
                .cheapestPrice(new BigDecimal("350.00"))
                .mostExpensivePrice(new BigDecimal("900.00"))
                .averagePrice(new BigDecimal("560.00"))
                .averagePricePerSqm(new BigDecimal("11.80"))
                .neighborhoodsTracked(8)
                .build();

        when(analyticsService.getCitySummary()).thenReturn(summary);

        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalListings").value(142))
                .andExpect(jsonPath("$.neighborhoodsTracked").value(8))
                .andExpect(jsonPath("$.averagePrice").value(560.00));
    }

    @Test
    void getCheapest_returns200_withSortedList() throws Exception {
        CheapestListingResponse listing = CheapestListingResponse.builder()
                .title("Studio in Annelinn")
                .price(new BigDecimal("280.00"))
                .size(new BigDecimal("22.00"))
                .pricePerSqm(new BigDecimal("12.73"))
                .neighborhood("Annelinn")
                .rooms(1)
                .build();

        when(analyticsService.getCheapest(any(), any())).thenReturn(List.of(listing));

        mockMvc.perform(get("/analytics/cheapest")
                        .param("maxPrice", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].neighborhood").value("Annelinn"))
                .andExpect(jsonPath("$[0].price").value(280.00));
    }

    @Test
    void compute_returns202_withSnapshotsCreated() throws Exception {
        when(analyticsService.computeAndSave()).thenReturn(Map.of(
                "snapshotsCreated", 8,
                "neighborhoods", 10,
                "date", LocalDate.now().toString()
        ));

        mockMvc.perform(post("/analytics/compute"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.snapshotsCreated").value(8));
    }

    // ─── Error case tests ──────────────────────────────────────────────────────

    @Test
    void getTrends_returns404_whenNeighborhoodHasNoData() throws Exception {
        when(analyticsService.getTrends(eq("UnknownPlace"), anyInt()))
                .thenThrow(new SnapshotNotFoundException("UnknownPlace"));

        mockMvc.perform(get("/analytics/trends")
                        .param("neighborhood", "UnknownPlace"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getSummary_returns500_whenServiceThrows() throws Exception {
        when(analyticsService.getCitySummary())
                .thenThrow(new RuntimeException("Database unavailable"));

        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
