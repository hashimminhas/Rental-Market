package com.uuriturg.neighborhood.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Neighborhood Service API")
                .version("1.0")
                .description("Tartu neighborhood profiles and resident reviews"));
    }
}
