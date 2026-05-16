package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new user")
public class CreateUserRequest {

    @NotBlank
    @Schema(description = "First name", example = "Jaan")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name", example = "Tamm")
    private String lastName;

    @NotBlank
    @Email
    @Schema(description = "Unique email address", example = "jaan.tamm@ut.ee")
    private String email;

    @Schema(description = "Phone number (optional)", example = "+372 5123 4567")
    private String phone;

    @Schema(description = "User role — defaults to TENANT if not provided", example = "TENANT",
            allowableValues = {"TENANT", "LANDLORD", "ADMIN"})
    private String role;
}
