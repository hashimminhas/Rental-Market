package com.uuriturg.notification.client;

import com.uuriturg.notification.dto.UserProfileDto;
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

    public UserProfileDto getUserById(UUID userId) {
        try {
            return userWebClient.get()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserProfileDto.class)
                    .block();
        } catch (Exception e) {
            log.warn("Could not fetch user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
