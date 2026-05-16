package com.uuriturg.neighborhood.repository;

import com.uuriturg.neighborhood.domain.Review;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IReviewRepository extends CrudRepository<Review, UUID> {

    List<Review> findByNeighborhoodId(UUID neighborhoodId);

    List<Review> findByUserId(UUID userId);

    boolean existsByNeighborhoodIdAndUserId(UUID neighborhoodId, UUID userId);
}
