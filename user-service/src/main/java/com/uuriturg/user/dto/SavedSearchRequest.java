package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for saving a search filter")
public class SavedSearchRequest {

    @Schema(description = "Preferred neighborhood", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Maximum monthly rent in EUR", example = "600")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum apartment size in m²", example = "40")
    private BigDecimal minSize;

    @Schema(description = "Minimum number of rooms", example = "2")
    private Integer minRooms;
}
