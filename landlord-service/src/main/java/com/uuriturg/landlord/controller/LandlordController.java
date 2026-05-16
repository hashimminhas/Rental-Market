package com.uuriturg.landlord.controller;

import com.uuriturg.landlord.dto.*;
import com.uuriturg.landlord.service.LandlordService;
import com.uuriturg.landlord.service.ReviewService;
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
@RequestMapping("/landlords")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Landlords", description = "Landlord profiles, tenant reviews, and reputation")
public class LandlordController {

    private final LandlordService landlordService;
    private final ReviewService reviewService;

    @Operation(summary = "List all landlord profiles")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "List returned")})
    @GetMapping
    public ResponseEntity<List<LandlordSummaryResponse>> getAllLandlords() {
        return ResponseEntity.ok(landlordService.getAllLandlords());
    }

    @Operation(summary = "Get top 10 highest-rated landlords")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Top 10 returned")})
    @GetMapping("/top")
    public ResponseEntity<List<LandlordSummaryResponse>> getTopRated() {
        return ResponseEntity.ok(landlordService.getTopRated());
    }

    @Operation(summary = "Get a landlord profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "404", description = "Landlord not found")
    })
    @GetMapping("/{landlordId}")
    public ResponseEntity<LandlordResponse> getById(
            @Parameter(description = "Landlord profile UUID") @PathVariable UUID landlordId) {
        return ResponseEntity.ok(landlordService.getById(landlordId));
    }

    @Operation(summary = "Get a landlord profile by user ID",
            description = "Looks up the landlord profile linked to the given user-service user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "404", description = "No landlord profile for this user")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<LandlordResponse> getByUserId(
            @Parameter(description = "User UUID from user-service") @PathVariable UUID userId) {
        return ResponseEntity.ok(landlordService.getByUserId(userId));
    }

    @Operation(summary = "Create a landlord profile",
            description = "Creates a profile for an existing active user. Validates the user via user-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or user not found/inactive")
    })
    @PostMapping
    public ResponseEntity<LandlordResponse> createLandlord(@Valid @RequestBody CreateLandlordRequest request) {
        return ResponseEntity.ok(landlordService.createLandlord(request));
    }

    @Operation(summary = "Update a landlord profile",
            description = "Updates displayName, bio, or phoneNumber. All fields are optional — only provided fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "404", description = "Landlord not found")
    })
    @PutMapping("/{landlordId}")
    public ResponseEntity<LandlordResponse> updateLandlord(
            @Parameter(description = "Landlord profile UUID") @PathVariable UUID landlordId,
            @RequestBody UpdateLandlordRequest request) {
        return ResponseEntity.ok(landlordService.updateLandlord(landlordId, request));
    }

    @Operation(summary = "Verify a landlord",
            description = "Marks the landlord as verified (isVerified=true). Represents an admin approval action.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Landlord verified"),
            @ApiResponse(responseCode = "404", description = "Landlord not found")
    })
    @PatchMapping("/{landlordId}/verify")
    public ResponseEntity<LandlordResponse> verifyLandlord(
            @Parameter(description = "Landlord profile UUID") @PathVariable UUID landlordId) {
        return ResponseEntity.ok(landlordService.verifyLandlord(landlordId));
    }

    @Operation(summary = "Submit a tenant review for a landlord",
            description = "Each user may review a landlord only once. The landlord's average rating is updated automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Landlord not found"),
            @ApiResponse(responseCode = "409", description = "User already reviewed this landlord")
    })
    @PostMapping("/reviews")
    public ResponseEntity<TenantReviewResponse> addReview(@Valid @RequestBody TenantReviewRequest request) {
        return ResponseEntity.ok(reviewService.addReview(request));
    }

    @Operation(summary = "Get all reviews for a landlord")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Review list returned")})
    @GetMapping("/{landlordId}/reviews")
    public ResponseEntity<List<TenantReviewResponse>> getReviewsForLandlord(
            @Parameter(description = "Landlord profile UUID") @PathVariable UUID landlordId) {
        return ResponseEntity.ok(reviewService.getReviewsForLandlord(landlordId));
    }
}
