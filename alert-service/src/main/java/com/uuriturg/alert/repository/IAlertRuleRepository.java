package com.uuriturg.alert.repository;

import com.uuriturg.alert.domain.AlertRule;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IAlertRuleRepository extends CrudRepository<AlertRule, UUID> {

    List<AlertRule> findByIsActiveTrue();

    List<AlertRule> findByUserId(UUID userId);

    List<AlertRule> findByUserIdAndIsActiveTrue(UUID userId);
}
