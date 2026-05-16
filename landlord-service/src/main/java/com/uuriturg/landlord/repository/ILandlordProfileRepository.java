package com.uuriturg.landlord.repository;

import com.uuriturg.landlord.domain.LandlordProfile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ILandlordProfileRepository extends CrudRepository<LandlordProfile, UUID> {

    Optional<LandlordProfile> findByUserId(UUID userId);

    List<LandlordProfile> findByIsVerifiedTrue();

    List<LandlordProfile> findTop10ByOrderByAverageRatingDesc();
}
