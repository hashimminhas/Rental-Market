package com.uuriturg.user.repository;

import com.uuriturg.user.domain.SavedSearch;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ISavedSearchRepository extends CrudRepository<SavedSearch, UUID> {

    List<SavedSearch> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
