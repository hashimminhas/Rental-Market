package com.uuriturg.landlord.repository;

import com.uuriturg.landlord.domain.TenantReview;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ITenantReviewRepository extends CrudRepository<TenantReview, UUID> {

    List<TenantReview> findByLandlordId(UUID landlordId);

    List<TenantReview> findByReviewerUserId(UUID reviewerUserId);

    boolean existsByLandlordIdAndReviewerUserId(UUID landlordId, UUID reviewerUserId);
}
