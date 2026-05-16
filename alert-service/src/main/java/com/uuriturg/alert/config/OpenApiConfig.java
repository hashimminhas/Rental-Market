package com.uuriturg.alert.config;

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
                        .title("Alert Service API")
                        .version("1.0")
                        .description("Manages alert rules for users. Consumes listing.new events from RabbitMQ, " +
                                "evaluates active rules, and triggers notifications via notification-service " +
                                "when a matching listing is found."));
    }
}
