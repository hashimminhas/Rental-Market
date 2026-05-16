package com.uuriturg.alert.service;

import com.uuriturg.alert.client.UserServiceClient;
import com.uuriturg.alert.domain.AlertRule;
import com.uuriturg.alert.dto.AlertRuleResponse;
import com.uuriturg.alert.dto.CreateAlertRequest;
import com.uuriturg.alert.dto.ValidateUserDto;
import com.uuriturg.alert.exception.AlertNotFoundException;
import com.uuriturg.alert.repository.IAlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleServiceImpl implements AlertRuleService {

    private final IAlertRuleRepository alertRuleRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public AlertRuleResponse createAlert(CreateAlertRequest request) {
        ValidateUserDto user = userServiceClient.validateUser(request.getUserId());
        if (user == null || !Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("User not found or inactive: " + request.getUserId());
        }

        AlertRule rule = AlertRule.builder()
                .userId(request.getUserId())
                .neighborhood(request.getNeighborhood())
                .maxPrice(request.getMaxPrice())
                .minSize(request.getMinSize())
                .minRooms(request.getMinRooms())
                .build();

        return toResponse(alertRuleRepository.save(rule));
    }

    @Override
    public List<AlertRuleResponse> getAllAlerts() {
        return StreamSupport.stream(alertRuleRepository.findAll().spliterator(), false)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AlertRuleResponse getAlertById(UUID alertId) {
        AlertRule rule = alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        return toResponse(rule);
    }

    @Override
    public void deactivateAlert(UUID alertId) {
        AlertRule rule = alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        rule.setIsActive(false);
        alertRuleRepository.save(rule);
    }

    private AlertRuleResponse toResponse(AlertRule rule) {
        return AlertRuleResponse.builder()
                .alertId(rule.getAlertId())
                .userId(rule.getUserId())
                .neighborhood(rule.getNeighborhood())
                .maxPrice(rule.getMaxPrice())
                .minSize(rule.getMinSize())
                .minRooms(rule.getMinRooms())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
