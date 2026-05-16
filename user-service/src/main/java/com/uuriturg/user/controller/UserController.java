package com.uuriturg.user.controller;

import com.uuriturg.user.dto.*;
import com.uuriturg.user.service.SavedSearchService;
import com.uuriturg.user.service.UserService;
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
@RequestMapping("/users")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User account management and saved searches")
public class UserController {

    private final UserService userService;
    private final SavedSearchService savedSearchService;

    @Operation(summary = "Create a new user account",
            description = "Registers a new tenant or landlord. Email must be unique — returns 409 if already taken.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing required fields"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "Get a user by ID",
            description = "Returns the full user profile. Returns 404 if not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(summary = "Update a user's profile",
            description = "Updates firstName, lastName, phone, or role. Email cannot be changed. Returns 404 if not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(summary = "Deactivate a user account",
            description = "Soft-deletes the user by setting active=false. The record is kept in the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validate a user",
            description = "Used by other services (alert, neighborhood, landlord, listing) to verify a user exists " +
                    "and is active before performing operations on their behalf. Returns 404 if not found or inactive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User is valid and active"),
            @ApiResponse(responseCode = "404", description = "User not found or inactive")
    })
    @GetMapping("/validate/{userId}")
    public ResponseEntity<ValidateUserResponse> validateUser(
            @Parameter(description = "User UUID to validate") @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.validateUser(userId));
    }

    @Operation(summary = "Get saved searches for a user",
            description = "Returns all saved search filters belonging to the user. Returns 404 if user does not exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of saved searches (empty if none)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/searches")
    public ResponseEntity<List<SavedSearchResponse>> getSavedSearches(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        return ResponseEntity.ok(savedSearchService.getSearchesForUser(userId));
    }

    @Operation(summary = "Save a new search filter for a user",
            description = "Saves a neighborhood/price/size/rooms filter for the user. Returns 404 if user does not exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search saved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/searches")
    public ResponseEntity<SavedSearchResponse> saveSearch(
            @Parameter(description = "User UUID") @PathVariable UUID userId,
            @RequestBody SavedSearchRequest request) {
        return ResponseEntity.ok(savedSearchService.saveSearch(userId, request));
    }
}
