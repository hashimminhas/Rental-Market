package com.uuriturg.neighborhood.client;

import com.uuriturg.neighborhood.dto.AnalyticsPriceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@Slf4j
public class AnalyticsClient {

    private final WebClient analyticsWebClient;

    public AnalyticsClient(@Qualifier("analyticsWebClient") WebClient analyticsWebClient) {
        this.analyticsWebClient = analyticsWebClient;
    }

    public AnalyticsPriceDto getPriceForNeighborhood(String neighborhood) {
        try {
            List<AnalyticsPriceDto> summaries = analyticsWebClient.get()
                    .uri("/analytics/summary")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AnalyticsPriceDto>>() {})
                    .block();

            if (summaries == null) return null;
            return summaries.stream()
                    .filter(s -> neighborhood.equalsIgnoreCase(s.getNeighborhood()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not fetch analytics data for neighborhood {}: {}", neighborhood, e.getMessage());
            return null;
        }
    }
}
