package com.uuriturg.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.alert.controller.AlertController;
import com.uuriturg.alert.dto.AlertMatchResponse;
import com.uuriturg.alert.dto.AlertRuleResponse;
import com.uuriturg.alert.dto.CreateAlertRequest;
import com.uuriturg.alert.exception.AlertNotFoundException;
import com.uuriturg.alert.service.AlertMatchService;
import com.uuriturg.alert.service.AlertRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertRuleService alertRuleService;

    @MockitoBean
    private AlertMatchService alertMatchService;

    private final UUID alertId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId  = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private AlertRuleResponse sampleRule() {
        return AlertRuleResponse.builder()
                .alertId(alertId)
                .userId(userId)
                .neighborhood("Kesklinn")
                .maxPrice(new BigDecimal("600"))
                .minSize(new BigDecimal("40"))
                .minRooms(2)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAlert_returns200() throws Exception {
        CreateAlertRequest req = CreateAlertRequest.builder()
                .userId(userId)
                .neighborhood("Kesklinn")
                .maxPrice(new BigDecimal("600"))
                .minSize(new BigDecimal("40"))
                .minRooms(2)
                .build();

        when(alertRuleService.createAlert(any())).thenReturn(sampleRule());

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(alertId.toString()))
                .andExpect(jsonPath("$.neighborhood").value("Kesklinn"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void createAlert_returns400_whenUserInvalid() throws Exception {
        CreateAlertRequest req = CreateAlertRequest.builder()
                .userId(userId)
                .maxPrice(new BigDecimal("600"))
                .build();

        when(alertRuleService.createAlert(any()))
                .thenThrow(new IllegalArgumentException("User not found or inactive: " + userId));

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAlerts_returns200() throws Exception {
        when(alertRuleService.getAllAlerts()).thenReturn(List.of(sampleRule()));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alertId").value(alertId.toString()));
    }

    @Test
    void getAlertById_returns200() throws Exception {
        when(alertRuleService.getAlertById(alertId)).thenReturn(sampleRule());

        mockMvc.perform(get("/alerts/{alertId}", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(alertId.toString()))
                .andExpect(jsonPath("$.maxPrice").value(600));
    }

    @Test
    void getAlertById_returns404_whenNotFound() throws Exception {
        when(alertRuleService.getAlertById(alertId))
                .thenThrow(new AlertNotFoundException(alertId));

        mockMvc.perform(get("/alerts/{alertId}", alertId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateAlert_returns204() throws Exception {
        doNothing().when(alertRuleService).deactivateAlert(alertId);

        mockMvc.perform(delete("/alerts/{alertId}", alertId))
                .andExpect(status().isNoContent());

        verify(alertRuleService, times(1)).deactivateAlert(alertId);
    }

    @Test
    void deactivateAlert_returns404_whenNotFound() throws Exception {
        doThrow(new AlertNotFoundException(alertId))
                .when(alertRuleService).deactivateAlert(alertId);

        mockMvc.perform(delete("/alerts/{alertId}", alertId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMatches_returns200() throws Exception {
        AlertMatchResponse match = AlertMatchResponse.builder()
                .matchId(UUID.randomUUID())
                .alertId(alertId)
                .listingId(UUID.randomUUID())
                .matchedAt(LocalDateTime.now())
                .notified(true)
                .build();

        when(alertRuleService.getAlertById(alertId)).thenReturn(sampleRule());
        when(alertMatchService.getMatchesForAlert(alertId)).thenReturn(List.of(match));

        mockMvc.perform(get("/alerts/{alertId}/matches", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alertId").value(alertId.toString()))
                .andExpect(jsonPath("$[0].notified").value(true));
    }

    @Test
    void getMatches_returns404_whenAlertNotFound() throws Exception {
        when(alertMatchService.getMatchesForAlert(alertId))
                .thenThrow(new AlertNotFoundException(alertId));

        mockMvc.perform(get("/alerts/{alertId}/matches", alertId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAlert_returns200() throws Exception {
        when(alertRuleService.getAlertById(alertId)).thenReturn(sampleRule());

        mockMvc.perform(post("/alerts/test/{alertId}", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(alertId.toString()));
    }
}
