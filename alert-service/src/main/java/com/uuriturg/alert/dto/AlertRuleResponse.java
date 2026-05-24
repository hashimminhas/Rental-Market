package com.uuriturg.alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An alert rule that fires when a matching listing is scraped")
public class AlertRuleResponse {

    @Schema(description = "Alert rule ID")
    private UUID alertId;

    @Schema(description = "Email address that will receive notifications", example = "you@example.com")
    private String email;

    @Schema(description = "Display name for this alert", example = "Budget flat in Karlova")
    private String name;

    @Schema(description = "Neighborhood filter (null = any)", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Minimum price filter in EUR", example = "200")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter in EUR", example = "600")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum size filter in m²", example = "40")
    private BigDecimal minSize;

    @Schema(description = "Minimum rooms filter", example = "2")
    private Integer minRooms;

    @Schema(description = "Whether this alert is active", example = "true")
    private Boolean isActive;

    @Schema(description = "When the alert was created")
    private LocalDateTime createdAt;
}
