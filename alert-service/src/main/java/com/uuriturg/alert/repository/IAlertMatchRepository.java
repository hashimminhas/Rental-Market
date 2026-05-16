package com.uuriturg.alert.repository;

import com.uuriturg.alert.domain.AlertMatch;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IAlertMatchRepository extends CrudRepository<AlertMatch, UUID> {

    List<AlertMatch> findByAlertId(UUID alertId);

    boolean existsByAlertIdAndListingId(UUID alertId, UUID listingId);
}
