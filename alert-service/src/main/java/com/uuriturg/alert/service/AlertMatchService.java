package com.uuriturg.alert.service;

import com.uuriturg.alert.dto.AlertMatchResponse;
import com.uuriturg.alert.dto.ListingEventDto;

import java.util.List;
import java.util.UUID;

public interface AlertMatchService {

    void evaluateAndMatch(ListingEventDto event);

    List<AlertMatchResponse> getMatchesForAlert(UUID alertId);
}
