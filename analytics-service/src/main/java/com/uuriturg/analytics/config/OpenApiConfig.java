package com.uuriturg.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analytics Service API")
                        .version("1.0")
                        .description("Computes price trends and neighborhood snapshots from scraped Tartu rental listings. " +
                                "Calls scraper-service for listing data and stores NeighborhoodSnapshot records."));
    }
}
