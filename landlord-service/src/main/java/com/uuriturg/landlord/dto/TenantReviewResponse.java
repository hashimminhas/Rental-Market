package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A tenant review for a landlord")
public class TenantReviewResponse {

    @Schema(description = "Review UUID")
    private UUID reviewId;

    @Schema(description = "Landlord profile that was reviewed")
    private UUID landlordId;

    @Schema(description = "User who submitted the review")
    private UUID reviewerUserId;

    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;

    @Schema(description = "Written comment")
    private String comment;

    @Schema(description = "When the review was submitted")
    private LocalDateTime createdAt;
}
