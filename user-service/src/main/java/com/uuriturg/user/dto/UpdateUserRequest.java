package com.uuriturg.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing user — all fields optional")
public class UpdateUserRequest {

    @Schema(description = "Updated first name", example = "Jaan")
    private String firstName;

    @Schema(description = "Updated last name", example = "Tamm")
    private String lastName;

    @Schema(description = "Updated phone number", example = "+372 5999 0000")
    private String phone;

    @Schema(description = "Updated role", example = "LANDLORD",
            allowableValues = {"TENANT", "LANDLORD", "ADMIN"})
    private String role;
}
