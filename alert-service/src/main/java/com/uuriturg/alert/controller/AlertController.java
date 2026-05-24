package com.uuriturg.alert.controller;

import com.uuriturg.alert.dto.*;
import com.uuriturg.alert.service.AlertMatchService;
import com.uuriturg.alert.service.AlertRuleService;
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
@RequestMapping("/alerts")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Manage alert rules and view matches")
public class AlertController {

    private final AlertRuleService alertRuleService;
    private final AlertMatchService alertMatchService;

    @Operation(summary = "Create a new alert rule",
            description = "Creates an alert for a user. Validates the user via user-service first. " +
                    "When a new listing is scraped that matches the rule, a notification is sent automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or user not found/inactive")
    })
    @PostMapping
    public ResponseEntity<AlertRuleResponse> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return ResponseEntity.ok(alertRuleService.createAlert(request));
    }

    @Operation(summary = "List all alert rules",
            description = "Returns all alert rules (active and inactive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    @GetMapping
    public ResponseEntity<List<AlertRuleResponse>> getAllAlerts() {
        return ResponseEntity.ok(alertRuleService.getAllAlerts());
    }

    @Operation(summary = "Get a single alert rule by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @GetMapping("/{alertId}")
    public ResponseEntity<AlertRuleResponse> getAlertById(
            @Parameter(description = "Alert rule UUID") @PathVariable UUID alertId) {
        return ResponseEntity.ok(alertRuleService.getAlertById(alertId));
    }

    @Operation(summary = "Deactivate an alert rule",
            description = "Sets isActive=false. The rule is kept in the database but will no longer match new listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alert deactivated"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deactivateAlert(
            @Parameter(description = "Alert rule UUID") @PathVariable UUID alertId) {
        alertRuleService.deactivateAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all matches for an alert",
            description = "Returns all listing matches that were found for this alert rule. " +
                    "Each match shows the listingId, when it was matched, and whether a notification was sent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match list returned"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @GetMapping("/{alertId}/matches")
    public ResponseEntity<List<AlertMatchResponse>> getMatches(
            @Parameter(description = "Alert rule UUID") @PathVariable UUID alertId) {
        return ResponseEntity.ok(alertMatchService.getMatchesForAlert(alertId));
    }

    @Operation(summary = "Manually test-fire an alert",
            description = "For demo purposes — immediately evaluates all active listings against this alert. " +
                    "Useful to demonstrate the matching logic without waiting for a new scrape.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test fired — check matches endpoint for results"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @PostMapping("/test/{alertId}")
    public ResponseEntity<AlertRuleResponse> testAlert(
            @Parameter(description = "Alert rule UUID to test") @PathVariable UUID alertId) {
        AlertRuleResponse rule = alertRuleService.getAlertById(alertId);
        alertMatchService.testFire(alertId);
        return ResponseEntity.ok(rule);
    }
}
