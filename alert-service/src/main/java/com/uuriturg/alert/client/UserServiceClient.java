package com.uuriturg.alert.client;

import com.uuriturg.alert.dto.ValidateUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient userWebClient;

    public UserServiceClient(@Qualifier("userWebClient") WebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

    public ValidateUserDto validateUser(UUID userId) {
        try {
            return userWebClient.get()
                    .uri("/users/validate/{userId}", userId)
                    .retrieve()
                    .bodyToMono(ValidateUserDto.class)
                    .block();
        } catch (Exception e) {
            log.warn("UserServiceClient: could not validate user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
