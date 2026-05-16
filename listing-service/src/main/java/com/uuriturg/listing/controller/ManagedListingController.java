package com.uuriturg.listing.controller;

import com.uuriturg.listing.dto.*;
import com.uuriturg.listing.service.ManagedListingService;
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
import java.util.UUID;

@RestController
@RequestMapping("/listings/managed")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Managed Listings", description = "Landlord-claimed listings and claim workflow")
public class ManagedListingController {

    private final ManagedListingService managedListingService;

    @Operation(summary = "List all managed listings",
            description = "Returns all listings that have been claimed by a landlord, across all statuses.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "List returned")})
    @GetMapping
    public ResponseEntity<List<ManagedListingSummaryResponse>> getAllManagedListings() {
        return ResponseEntity.ok(managedListingService.getAllManagedListings());
    }

    @Operation(summary = "List available managed listings",
            description = "Returns AVAILABLE listings only. Filter by neighborhood using the optional query parameter.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "List returned")})
    @GetMapping("/available")
    public ResponseEntity<List<ManagedListingSummaryResponse>> getAvailable(
            @Parameter(description = "Optional neighborhood filter e.g. Kesklinn") @RequestParam(required = false) String neighborhood) {
        return ResponseEntity.ok(managedListingService.getAvailable(neighborhood));
    }

    @Operation(summary = "Get all managed listings for a landlord")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "List returned")})
    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<ManagedListingResponse>> getByLandlord(
            @Parameter(description = "Landlord profile UUID") @PathVariable UUID landlordId) {
        return ResponseEntity.ok(managedListingService.getByLandlord(landlordId));
    }

    @Operation(summary = "Get a managed listing by its scraper listing ID",
            description = "Checks whether a specific scraper-service listing has already been claimed, and returns the managed record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed listing found"),
            @ApiResponse(responseCode = "404", description = "Not yet claimed")
    })
    @GetMapping("/scraper/{scrapedListingId}")
    public ResponseEntity<ManagedListingResponse> getByScrapedListingId(
            @Parameter(description = "Scraper listing UUID") @PathVariable UUID scrapedListingId) {
        return ResponseEntity.ok(managedListingService.getByScrapedListingId(scrapedListingId));
    }

    @Operation(summary = "Get a managed listing by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing returned"),
            @ApiResponse(responseCode = "404", description = "Managed listing not found")
    })
    @GetMapping("/{managedListingId}")
    public ResponseEntity<ManagedListingResponse> getById(
            @Parameter(description = "Managed listing UUID") @PathVariable UUID managedListingId) {
        return ResponseEntity.ok(managedListingService.getById(managedListingId));
    }

    @Operation(summary = "Claim a scraper listing",
            description = "A landlord claims ownership of a scraped listing. " +
                    "Fetches original data from scraper-service to populate defaults. " +
                    "Publishes a listing.claimed event on success.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing claimed"),
            @ApiResponse(responseCode = "400", description = "Validation failed or landlord not found"),
            @ApiResponse(responseCode = "409", description = "Listing already claimed by another landlord")
    })
    @PostMapping("/claim")
    public ResponseEntity<ManagedListingResponse> claimListing(@Valid @RequestBody ClaimListingRequest request) {
        return ResponseEntity.ok(managedListingService.claimListing(request));
    }

    @Operation(summary = "Update a managed listing",
            description = "Update title, description, price, size, rooms, address, or status. All fields optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing updated"),
            @ApiResponse(responseCode = "404", description = "Managed listing not found")
    })
    @PutMapping("/{managedListingId}")
    public ResponseEntity<ManagedListingResponse> updateListing(
            @Parameter(description = "Managed listing UUID") @PathVariable UUID managedListingId,
            @RequestBody UpdateManagedListingRequest request) {
        return ResponseEntity.ok(managedListingService.updateListing(managedListingId, request));
    }

    @Operation(summary = "Withdraw a managed listing",
            description = "Sets the listing status to WITHDRAWN. The record is kept but the listing is no longer shown as available.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing withdrawn"),
            @ApiResponse(responseCode = "404", description = "Managed listing not found")
    })
    @PatchMapping("/{managedListingId}/withdraw")
    public ResponseEntity<ManagedListingResponse> withdrawListing(
            @Parameter(description = "Managed listing UUID") @PathVariable UUID managedListingId) {
        return ResponseEntity.ok(managedListingService.withdrawListing(managedListingId));
    }
}
