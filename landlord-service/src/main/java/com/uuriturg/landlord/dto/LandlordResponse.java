package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full landlord profile")
public class LandlordResponse {

    @Schema(description = "Landlord profile UUID")
    private UUID landlordId;

    @Schema(description = "Linked user-service user ID")
    private UUID userId;

    @Schema(description = "Public display name", example = "Jaan Tamm")
    private String displayName;

    @Schema(description = "Bio or description")
    private String bio;

    @Schema(description = "Contact phone number", example = "+372 5555 1234")
    private String phoneNumber;

    @Schema(description = "Whether this landlord has been verified", example = "true")
    private Boolean isVerified;

    @Schema(description = "Average tenant rating (1–5)", example = "4.3")
    private Double averageRating;

    @Schema(description = "Total number of tenant reviews", example = "9")
    private Integer reviewCount;

    @Schema(description = "When the profile was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the profile was last updated")
    private LocalDateTime updatedAt;
}
