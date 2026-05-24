package com.uuriturg.scraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single price snapshot for a listing")
public class PriceHistoryResponse {

    @Schema(description = "Monthly rent in EUR at this point in time", example = "550.00")
    private BigDecimal price;

    @Schema(description = "When this price was recorded")
    private LocalDateTime recordedAt;
}
