package com.uuriturg.user.service;

import com.uuriturg.user.dto.SavedSearchRequest;
import com.uuriturg.user.dto.SavedSearchResponse;

import java.util.List;
import java.util.UUID;

public interface SavedSearchService {

    List<SavedSearchResponse> getSearchesForUser(UUID userId);

    SavedSearchResponse saveSearch(UUID userId, SavedSearchRequest request);
}
