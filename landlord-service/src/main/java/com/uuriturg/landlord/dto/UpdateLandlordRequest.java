package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a landlord profile — all fields optional")
public class UpdateLandlordRequest {

    @Schema(description = "Updated display name", example = "Jaan Tamm")
    private String displayName;

    @Schema(description = "Updated bio", example = "Now managing 8 properties in Tartu")
    private String bio;

    @Schema(description = "Updated phone number", example = "+372 5555 9999")
    private String phoneNumber;
}
