package com.uuriturg.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for submitting a neighborhood review")
public class ReviewRequest {

    @NotNull
    @Schema(description = "User submitting the review", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @NotNull
    @Schema(description = "Neighborhood being reviewed", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID neighborhoodId;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Rating from 1 (poor) to 5 (excellent)", example = "4")
    private Integer rating;

    @Schema(description = "Optional written comment", example = "Great area, very walkable!")
    private String comment;
}
