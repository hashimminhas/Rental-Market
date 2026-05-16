package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full user profile returned by GET and POST /users")
public class UserResponse {

    @Schema(description = "Unique user identifier")
    private UUID userId;

    @Schema(description = "First name", example = "Jaan")
    private String firstName;

    @Schema(description = "Last name", example = "Tamm")
    private String lastName;

    @Schema(description = "Email address", example = "jaan.tamm@ut.ee")
    private String email;

    @Schema(description = "Phone number", example = "+372 5123 4567")
    private String phone;

    @Schema(description = "User role", example = "TENANT")
    private String role;

    @Schema(description = "Whether the user account is active", example = "true")
    private Boolean active;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
