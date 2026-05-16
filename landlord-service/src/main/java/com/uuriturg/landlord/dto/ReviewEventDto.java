package com.uuriturg.landlord.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEventDto {

    private UUID landlordId;
    private String landlordDisplayName;
    private UUID reviewerUserId;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewedAt;
}
