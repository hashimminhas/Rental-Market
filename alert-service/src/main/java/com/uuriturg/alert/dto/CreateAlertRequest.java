package com.uuriturg.alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new alert rule")
public class CreateAlertRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address to notify when a match is found", example = "you@example.com")
    private String email;

    @Schema(description = "Display name for this alert", example = "Budget flat in Karlova")
    private String name;

    @Schema(description = "Neighborhood filter — null means any", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Minimum monthly rent in EUR", example = "200")
    private BigDecimal minPrice;

    @Schema(description = "Maximum monthly rent in EUR", example = "600")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum apartment size in m²", example = "40")
    private BigDecimal minSize;

    @Schema(description = "Minimum number of rooms", example = "2")
    private Integer minRooms;
}
