package com.uuriturg.neighborhood.service;

import com.uuriturg.neighborhood.dto.CreateNeighborhoodRequest;
import com.uuriturg.neighborhood.dto.NeighborhoodResponse;
import com.uuriturg.neighborhood.dto.NeighborhoodSummaryResponse;

import java.util.List;

public interface NeighborhoodService {

    List<NeighborhoodSummaryResponse> getAllNeighborhoods();

    NeighborhoodResponse getNeighborhoodBySlug(String slug);

    NeighborhoodResponse createNeighborhood(CreateNeighborhoodRequest request);

    int seedDefaultNeighborhoods();
}
