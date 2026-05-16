package com.uuriturg.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new neighborhood profile")
public class CreateNeighborhoodRequest {

    @NotBlank
    @Schema(description = "Neighborhood name", example = "Kesklinn")
    private String name;

    @Schema(description = "Short description of the neighborhood", example = "The city centre of Tartu with shops and cafes")
    private String description;

    @Schema(description = "Distance from city centre in km", example = "0.5")
    private Double distanceToCenter;

    @Schema(description = "Comma-separated characteristics", example = "lively,central,walkable")
    private String characteristics;
}
