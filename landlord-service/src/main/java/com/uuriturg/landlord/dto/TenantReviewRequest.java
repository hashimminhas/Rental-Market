package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for submitting a tenant review for a landlord")
public class TenantReviewRequest {

    @NotNull
    @Schema(description = "User ID of the tenant leaving the review", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID reviewerUserId;

    @NotNull
    @Schema(description = "Landlord profile ID being reviewed", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID landlordId;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Rating from 1 (very poor) to 5 (excellent)", example = "4")
    private Integer rating;

    @Schema(description = "Optional written comment", example = "Very responsive and professional landlord.")
    private String comment;
}
