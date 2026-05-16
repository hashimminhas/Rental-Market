package com.uuriturg.listing.dto;

import com.uuriturg.listing.domain.ManagedListingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a managed listing — all fields optional")
public class UpdateManagedListingRequest {

    @Schema(description = "Updated title", example = "Bright 2-room flat in Kesklinn")
    private String title;

    @Schema(description = "Updated description")
    private String description;

    @Schema(description = "Updated monthly rent in EUR", example = "575.00")
    private BigDecimal price;

    @Schema(description = "Updated size in m²", example = "52.0")
    private BigDecimal size;

    @Schema(description = "Updated room count", example = "2")
    private Integer rooms;

    @Schema(description = "Updated address", example = "Raatuse 22, Tartu")
    private String address;

    @Schema(description = "Updated status", example = "RENTED")
    private ManagedListingStatus status;
}
