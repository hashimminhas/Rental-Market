package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight landlord summary for list views")
public class LandlordSummaryResponse {

    @Schema(description = "Landlord profile UUID")
    private UUID landlordId;

    @Schema(description = "Public display name", example = "Jaan Tamm")
    private String displayName;

    @Schema(description = "Whether this landlord has been verified", example = "true")
    private Boolean isVerified;

    @Schema(description = "Average tenant rating (1–5)", example = "4.3")
    private Double averageRating;

    @Schema(description = "Total number of tenant reviews", example = "9")
    private Integer reviewCount;
}
