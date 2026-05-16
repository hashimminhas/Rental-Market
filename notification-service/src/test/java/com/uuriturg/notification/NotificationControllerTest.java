package com.uuriturg.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.notification.controller.NotificationController;
import com.uuriturg.notification.domain.NotificationChannel;
import com.uuriturg.notification.domain.NotificationStatus;
import com.uuriturg.notification.dto.NotificationResponse;
import com.uuriturg.notification.dto.SendNotificationRequest;
import com.uuriturg.notification.exception.NotificationNotFoundException;
import com.uuriturg.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private final UUID notificationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId         = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private NotificationResponse sampleResponse() {
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .recipientUserId(userId)
                .recipientEmail("tenant@example.com")
                .channel(NotificationChannel.EMAIL)
                .subject("New matching listing found!")
                .body("A listing matches your alert.")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendNotification_returns200() throws Exception {
        SendNotificationRequest req = SendNotificationRequest.builder()
                .recipientUserId(userId)
                .channel("EMAIL")
                .subject("New matching listing found!")
                .body("A listing matches your alert.")
                .build();

        when(notificationService.send(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    @Test
    void sendNotification_returns400_whenMissingFields() throws Exception {
        SendNotificationRequest req = SendNotificationRequest.builder()
                .recipientUserId(userId)
                // missing channel, subject, body
                .build();

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        when(notificationService.getById(notificationId)).thenReturn(sampleResponse());

        mockMvc.perform(get("/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$.recipientEmail").value("tenant@example.com"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(notificationService.getById(notificationId))
                .thenThrow(new NotificationNotFoundException(notificationId));

        mockMvc.perform(get("/notifications/{id}", notificationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByRecipient_returns200() throws Exception {
        when(notificationService.getByRecipient(userId)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipientUserId").value(userId.toString()));
    }

    @Test
    void getPending_returns200() throws Exception {
        NotificationResponse pending = sampleResponse();
        pending.setStatus(NotificationStatus.PENDING);
        pending.setSentAt(null);

        when(notificationService.getPending()).thenReturn(List.of(pending));

        mockMvc.perform(get("/notifications/status/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void retryNotification_returns200() throws Exception {
        when(notificationService.retry(notificationId)).thenReturn(sampleResponse());

        mockMvc.perform(post("/notifications/{id}/retry", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void retryNotification_returns404_whenNotFound() throws Exception {
        when(notificationService.retry(notificationId))
                .thenThrow(new NotificationNotFoundException(notificationId));

        mockMvc.perform(post("/notifications/{id}/retry", notificationId))
                .andExpect(status().isNotFound());
    }
}
