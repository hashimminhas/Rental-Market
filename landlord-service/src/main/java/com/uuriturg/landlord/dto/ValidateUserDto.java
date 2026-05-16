package com.uuriturg.landlord.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserDto {

    private UUID userId;
    private String email;
    private String role;
    private Boolean active;
}
