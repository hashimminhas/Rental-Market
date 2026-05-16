package com.uuriturg.neighborhood.service;

import com.uuriturg.neighborhood.dto.ReviewRequest;
import com.uuriturg.neighborhood.dto.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewResponse addReview(ReviewRequest request);

    List<ReviewResponse> getReviewsForNeighborhood(UUID neighborhoodId);

    List<ReviewResponse> getReviewsByUser(UUID userId);
}
