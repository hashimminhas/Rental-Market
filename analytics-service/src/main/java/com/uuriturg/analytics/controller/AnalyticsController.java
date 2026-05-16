package com.uuriturg.analytics.controller;

import com.uuriturg.analytics.dto.*;
import com.uuriturg.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Neighborhood price trends and rental market summaries for Tartu")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Get latest price snapshot per neighborhood",
            description = "Returns the most recent NeighborhoodSnapshot for each of the 10 tracked Tartu neighborhoods. " +
                    "Includes average price, price per m², listing count, and price change vs previous snapshot."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Snapshots returned (empty list if no data yet — run POST /analytics/compute first)")
    })
    @GetMapping("/neighborhoods")
    public ResponseEntity<List<NeighborhoodSummaryResponse>> getNeighborhoods() {
        return ResponseEntity.ok(analyticsService.getLatestPerNeighborhood());
    }

    @Operation(
            summary = "Get price trend for a neighborhood",
            description = "Returns daily price snapshots for the given neighborhood over the specified number of days. " +
                    "Use this to draw a price-over-time chart. Returns 404 if no data exists for the neighborhood."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trend data returned"),
            @ApiResponse(responseCode = "404", description = "No snapshot data found for the given neighborhood")
    })
    @GetMapping("/trends")
    public ResponseEntity<TrendResponse> getTrends(
            @Parameter(description = "Tartu neighborhood name, e.g. Kesklinn", required = true)
            @RequestParam String neighborhood,
            @Parameter(description = "Number of days to look back (default 30)")
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getTrends(neighborhood, days));
    }

    @Operation(
            summary = "Get city-wide rental market summary",
            description = "Aggregates the latest snapshots across all neighborhoods into one city-wide summary: " +
                    "total listings, cheapest/most expensive average neighborhood price, city average price and price per m²."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "City summary returned")
    })
    @GetMapping("/summary")
    public ResponseEntity<CitySummaryResponse> getSummary() {
        return ResponseEntity.ok(analyticsService.getCitySummary());
    }

    @Operation(
            summary = "Get top 10 cheapest listings",
            description = "Calls scraper-service and returns the 10 cheapest active listings sorted by price ascending. " +
                    "Optionally filter by neighborhood and/or maximum price."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top 10 cheapest listings returned (fewer if not enough data)")
    })
    @GetMapping("/cheapest")
    public ResponseEntity<List<CheapestListingResponse>> getCheapest(
            @Parameter(description = "Filter by neighborhood, e.g. Annelinn")
            @RequestParam(required = false) String neighborhood,
            @Parameter(description = "Maximum price in EUR, e.g. 500")
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(analyticsService.getCheapest(neighborhood, maxPrice));
    }

    @Operation(
            summary = "Trigger analytics recomputation",
            description = "Calls scraper-service for each of the 10 Tartu neighborhoods, computes price statistics, " +
                    "and saves a new NeighborhoodSnapshot for today. Returns count of snapshots created. " +
                    "Run this after a scrape to get up-to-date analytics."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Computation complete — snapshots saved"),
            @ApiResponse(responseCode = "500", description = "Computation failed")
    })
    @PostMapping("/compute")
    public ResponseEntity<Map<String, Object>> compute() {
        return ResponseEntity.accepted().body(analyticsService.computeAndSave());
    }
}
