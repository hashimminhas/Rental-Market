package com.uuriturg.alert.service;

import com.uuriturg.alert.domain.AlertRule;
import com.uuriturg.alert.dto.AlertRuleResponse;
import com.uuriturg.alert.dto.CreateAlertRequest;
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
    private final AlertMatchService alertMatchService;

    @Override
    public AlertRuleResponse createAlert(CreateAlertRequest request) {
        AlertRule rule = AlertRule.builder()
                .email(request.getEmail())
                .name(request.getName())
                .neighborhood(request.getNeighborhood())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .minSize(request.getMinSize())
                .minRooms(request.getMinRooms())
                .build();

        AlertRule saved = alertRuleRepository.save(rule);
        log.info("Alert created for {} — alertId={}", saved.getEmail(), saved.getAlertId());

        // scan existing listings in background — don't block the HTTP response
        UUID alertId = saved.getAlertId();
        Thread.ofVirtual().start(() -> {
            try {
                alertMatchService.scanExistingListings(alertId);
            } catch (Exception e) {
                log.warn("Initial scan failed for alert {}: {}", alertId, e.getMessage());
            }
        });

        return toResponse(saved);
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
                .email(rule.getEmail())
                .name(rule.getName())
                .neighborhood(rule.getNeighborhood())
                .minPrice(rule.getMinPrice())
                .maxPrice(rule.getMaxPrice())
                .minSize(rule.getMinSize())
                .minRooms(rule.getMinRooms())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
