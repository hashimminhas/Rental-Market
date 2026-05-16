package com.uuriturg.alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A match record created when a scraped listing satisfies an alert rule")
public class AlertMatchResponse {

    @Schema(description = "Match ID")
    private UUID matchId;

    @Schema(description = "The alert rule that was triggered")
    private UUID alertId;

    @Schema(description = "ID of the matching listing in scraper-service")
    private UUID listingId;

    @Schema(description = "When the match was recorded")
    private LocalDateTime matchedAt;

    @Schema(description = "Whether a notification was sent for this match", example = "true")
    private Boolean notified;
}
