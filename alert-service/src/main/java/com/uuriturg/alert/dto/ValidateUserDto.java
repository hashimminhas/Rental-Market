package com.uuriturg.alert.dto;

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
