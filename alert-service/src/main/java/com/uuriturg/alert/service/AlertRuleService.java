package com.uuriturg.alert.service;

import com.uuriturg.alert.dto.AlertRuleResponse;
import com.uuriturg.alert.dto.CreateAlertRequest;

import java.util.List;
import java.util.UUID;

public interface AlertRuleService {

    AlertRuleResponse createAlert(CreateAlertRequest request);

    List<AlertRuleResponse> getAllAlerts();

    AlertRuleResponse getAlertById(UUID alertId);

    void deactivateAlert(UUID alertId);
}
