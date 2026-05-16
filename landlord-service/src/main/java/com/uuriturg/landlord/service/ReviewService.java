package com.uuriturg.landlord.service;

import com.uuriturg.landlord.dto.TenantReviewRequest;
import com.uuriturg.landlord.dto.TenantReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    TenantReviewResponse addReview(TenantReviewRequest request);

    List<TenantReviewResponse> getReviewsForLandlord(UUID landlordId);

    List<TenantReviewResponse> getReviewsByUser(UUID reviewerUserId);
}
