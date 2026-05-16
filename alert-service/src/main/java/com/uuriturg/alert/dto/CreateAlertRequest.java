package com.uuriturg.alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new alert rule")
public class CreateAlertRequest {

    @NotNull
    @Schema(description = "ID of the user who owns this alert", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Neighborhood filter — null means any neighborhood", example = "Kesklinn")
    private String neighborhood;

    @NotNull
    @Schema(description = "Maximum monthly rent in EUR", example = "600")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum apartment size in m²", example = "40")
    private BigDecimal minSize;

    @Schema(description = "Minimum number of rooms", example = "2")
    private Integer minRooms;
}
