package com.uuriturg.landlord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a landlord profile")
public class CreateLandlordRequest {

    @NotNull
    @Schema(description = "User ID from user-service who owns this landlord profile", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @NotBlank
    @Schema(description = "Public display name", example = "Jaan Tamm")
    private String displayName;

    @Schema(description = "Short bio or description", example = "Experienced landlord in Tartu with 5+ properties")
    private String bio;

    @Schema(description = "Contact phone number", example = "+372 5555 1234")
    private String phoneNumber;
}
