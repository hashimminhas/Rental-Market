package com.uuriturg.notification.controller;

import com.uuriturg.notification.dto.NotificationResponse;
import com.uuriturg.notification.dto.SendNotificationRequest;
import com.uuriturg.notification.service.NotificationService;
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
@RequestMapping("/notifications")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Email and in-app notification delivery")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Send a notification",
            description = "Primary REST entry point called by other services (e.g. alert-service). " +
                    "Looks up the recipient's email from user-service and delivers via the requested channel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(notificationService.send(request));
    }

    @Operation(summary = "Get a notification by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification returned"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getById(
            @Parameter(description = "Notification UUID") @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.getById(notificationId));
    }

    @Operation(summary = "Get all notifications for a user",
            description = "Returns all notification records for the given user ID, across all channels and statuses.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification list returned")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(
            @Parameter(description = "Recipient user UUID") @PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getByRecipient(userId));
    }

    @Operation(summary = "Get all pending notifications",
            description = "Returns notifications with status=PENDING. Useful for admin monitoring and manual retry triggers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending list returned")
    })
    @GetMapping("/status/pending")
    public ResponseEntity<List<NotificationResponse>> getPending() {
        return ResponseEntity.ok(notificationService.getPending());
    }

    @Operation(summary = "Retry a failed notification",
            description = "Re-attempts delivery of a FAILED or PENDING notification. Updates status to SENT or FAILED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retry attempted — check status field for result"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<NotificationResponse> retry(
            @Parameter(description = "Notification UUID") @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.retry(notificationId));
    }
}
