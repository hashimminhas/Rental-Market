package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A saved search filter belonging to a user")
public class SavedSearchResponse {

    @Schema(description = "Saved search ID")
    private UUID searchId;

    @Schema(description = "Owner user ID")
    private UUID userId;

    @Schema(description = "Preferred neighborhood", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Maximum monthly rent in EUR", example = "600")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum apartment size in m²", example = "40")
    private BigDecimal minSize;

    @Schema(description = "Minimum number of rooms", example = "2")
    private Integer minRooms;

    @Schema(description = "When this search was saved")
    private LocalDateTime createdAt;
}
