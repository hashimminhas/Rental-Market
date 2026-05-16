package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight validation response used by other services to verify a user exists and is active")
public class ValidateUserResponse {

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Email address", example = "jaan.tamm@ut.ee")
    private String email;

    @Schema(description = "User role", example = "TENANT")
    private String role;

    @Schema(description = "Whether the user is active", example = "true")
    private Boolean active;
}
