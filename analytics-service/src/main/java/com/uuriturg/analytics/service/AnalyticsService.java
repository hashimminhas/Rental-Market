package com.uuriturg.analytics.service;

import com.uuriturg.analytics.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    List<NeighborhoodSummaryResponse> getLatestPerNeighborhood();

    TrendResponse getTrends(String neighborhood, int days);

    CitySummaryResponse getCitySummary();

    List<CheapestListingResponse> getCheapest(String neighborhood, BigDecimal maxPrice);

    Map<String, Object> computeAndSave();
}
