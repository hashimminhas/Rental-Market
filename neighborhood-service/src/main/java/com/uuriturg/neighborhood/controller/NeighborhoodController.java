package com.uuriturg.neighborhood.controller;

import com.uuriturg.neighborhood.client.AnalyticsClient;
import com.uuriturg.neighborhood.dto.*;
import com.uuriturg.neighborhood.service.NeighborhoodService;
import com.uuriturg.neighborhood.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/neighborhoods")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Neighborhoods", description = "Tartu neighborhood profiles and resident reviews")
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;
    private final ReviewService reviewService;
    private final AnalyticsClient analyticsClient;

    @Operation(summary = "List all neighborhoods",
            description = "Returns a summary of all 10 Tartu neighborhoods with average ratings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    @GetMapping
    public ResponseEntity<List<NeighborhoodSummaryResponse>> getAllNeighborhoods() {
        return ResponseEntity.ok(neighborhoodService.getAllNeighborhoods());
    }

    @Operation(summary = "Get neighborhood profile by slug",
            description = "Returns the full profile including live price data from analytics-service and aggregated review rating.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "404", description = "Neighborhood not found")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<NeighborhoodResponse> getBySlug(
            @Parameter(description = "Neighborhood slug e.g. kesklinn") @PathVariable String slug) {
        return ResponseEntity.ok(neighborhoodService.getNeighborhoodBySlug(slug));
    }

    @Operation(summary = "Create a neighborhood profile",
            description = "Adds a new neighborhood. The slug is auto-generated from the name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Neighborhood created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<NeighborhoodResponse> createNeighborhood(
            @Valid @RequestBody CreateNeighborhoodRequest request) {
        return ResponseEntity.ok(neighborhoodService.createNeighborhood(request));
    }

    @Operation(summary = "Seed all 10 Tartu default neighborhoods",
            description = "Inserts all 10 named districts with preset descriptions. Safe to call multiple times — skips existing entries.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seeding complete")
    })
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedNeighborhoods() {
        int seeded = neighborhoodService.seedDefaultNeighborhoods();
        return ResponseEntity.status(201).body(Map.of(
                "message", "Seeding complete",
                "inserted", seeded
        ));
    }

    @Operation(summary = "Get all reviews for a neighborhood")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review list returned")
    })
    @GetMapping("/{neighborhoodId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @Parameter(description = "Neighborhood UUID") @PathVariable UUID neighborhoodId) {
        return ResponseEntity.ok(reviewService.getReviewsForNeighborhood(neighborhoodId));
    }

    @Operation(summary = "Submit a review for a neighborhood",
            description = "Each user may submit only one review per neighborhood. Rating must be 1–5.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Neighborhood not found"),
            @ApiResponse(responseCode = "409", description = "User already reviewed this neighborhood")
    })
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.addReview(request));
    }

    @Operation(summary = "Get all reviews submitted by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review list returned")
    })
    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @Operation(summary = "Get live price data for a neighborhood",
            description = "Fetches average rent and price per m² from analytics-service for the given neighborhood slug.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price data returned (may be null if analytics unavailable)"),
            @ApiResponse(responseCode = "404", description = "Neighborhood not found")
    })
    @GetMapping("/{slug}/price")
    public ResponseEntity<AnalyticsPriceDto> getPriceData(
            @Parameter(description = "Neighborhood slug e.g. kesklinn") @PathVariable String slug) {
        neighborhoodService.getNeighborhoodBySlug(slug); // validates existence
        String name = slug.substring(0, 1).toUpperCase() + slug.substring(1);
        return ResponseEntity.ok(analyticsClient.getPriceForNeighborhood(name));
    }
}
