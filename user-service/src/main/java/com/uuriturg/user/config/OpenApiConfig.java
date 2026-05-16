package com.uuriturg.user.config;

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
                        .title("User Service API")
                        .version("1.0")
                        .description("Central identity provider for Üüriturg. Manages tenant and landlord accounts, " +
                                "saved searches, and provides a validate endpoint used by alert, neighborhood, " +
                                "landlord, and listing services."));
    }
}
