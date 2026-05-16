package com.uuriturg.notification.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;
}
