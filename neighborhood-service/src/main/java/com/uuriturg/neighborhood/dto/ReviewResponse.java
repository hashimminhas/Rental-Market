package com.uuriturg.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A resident review for a neighborhood")
public class ReviewResponse {

    @Schema(description = "Review UUID")
    private UUID reviewId;

    @Schema(description = "Neighborhood that was reviewed")
    private UUID neighborhoodId;

    @Schema(description = "User who submitted the review")
    private UUID userId;

    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;

    @Schema(description = "Written comment", example = "Great area, very walkable!")
    private String comment;

    @Schema(description = "When the review was submitted")
    private LocalDateTime createdAt;
}
