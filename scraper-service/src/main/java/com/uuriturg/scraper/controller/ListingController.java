package com.uuriturg.scraper.controller;

import com.uuriturg.scraper.dto.ListingResponse;
import com.uuriturg.scraper.service.ListingService;
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
import java.util.UUID;

@RestController
@RequestMapping("/listings")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Browse and retrieve scraped rental listings from KV.ee and City24")
public class ListingController {

    private final ListingService listingService;

    @Operation(
            summary = "Get all active listings",
            description = "Returns all active listings. Optionally filter by neighborhood name, maximum price (EUR), or minimum size (m²). All filters are optional and combinable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matching listings (empty list if none match)")
    })
    @GetMapping
    public ResponseEntity<List<ListingResponse>> getAllListings(
            @Parameter(description = "Tartu neighborhood name, e.g. Kesklinn, Annelinn, Karlova")
            @RequestParam(required = false) String neighborhood,
            @Parameter(description = "Maximum monthly rent in EUR, e.g. 600")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum apartment size in m², e.g. 40")
            @RequestParam(required = false) BigDecimal minSize) {
        return ResponseEntity.ok(listingService.findAll(neighborhood, maxPrice, minSize));
    }

    @Operation(
            summary = "Get the 50 most recently scraped listings",
            description = "Returns the 50 listings with the most recent scrapedAt timestamp, regardless of filters. Useful for seeing what was found in the last scrape run."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Up to 50 listings ordered by scrape time descending")
    })
    @GetMapping("/latest")
    public ResponseEntity<List<ListingResponse>> getLatestListings() {
        return ResponseEntity.ok(listingService.findLatest());
    }

    @Operation(
            summary = "Get a single listing by ID",
            description = "Returns full details for one listing. Returns 404 if the listing does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing found"),
            @ApiResponse(responseCode = "404", description = "Listing not found for the given ID")
    })
    @GetMapping("/{listingId}")
    public ResponseEntity<ListingResponse> getListingById(
            @Parameter(description = "UUID of the listing") @PathVariable UUID listingId) {
        return ResponseEntity.ok(listingService.findById(listingId));
    }
}
