package com.uuriturg.user.service;

import com.uuriturg.user.domain.SavedSearch;
import com.uuriturg.user.dto.SavedSearchRequest;
import com.uuriturg.user.dto.SavedSearchResponse;
import com.uuriturg.user.exception.UserNotFoundException;
import com.uuriturg.user.repository.ISavedSearchRepository;
import com.uuriturg.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedSearchServiceImpl implements SavedSearchService {

    private final ISavedSearchRepository savedSearchRepository;
    private final IUserRepository userRepository;

    @Override
    public List<SavedSearchResponse> getSearchesForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return savedSearchRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SavedSearchResponse saveSearch(UUID userId, SavedSearchRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        SavedSearch search = SavedSearch.builder()
                .userId(userId)
                .neighborhood(request.getNeighborhood())
                .maxPrice(request.getMaxPrice())
                .minSize(request.getMinSize())
                .minRooms(request.getMinRooms())
                .build();

        return toResponse(savedSearchRepository.save(search));
    }

    private SavedSearchResponse toResponse(SavedSearch s) {
        return SavedSearchResponse.builder()
                .searchId(s.getSearchId())
                .userId(s.getUserId())
                .neighborhood(s.getNeighborhood())
                .maxPrice(s.getMaxPrice())
                .minSize(s.getMinSize())
                .minRooms(s.getMinRooms())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
