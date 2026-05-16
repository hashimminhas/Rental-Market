package com.uuriturg.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Price trend data for one neighborhood over a time window")
public class TrendResponse {

    @Schema(description = "Neighborhood name", example = "Tammelinn")
    private String neighborhood;

    @Schema(description = "Number of days the trend covers", example = "30")
    private Integer days;

    @Schema(description = "Number of data points returned", example = "5")
    private Integer dataPoints;

    @Schema(description = "Ordered list of daily price snapshots (oldest first)")
    private List<TrendDataPoint> trend;
}
