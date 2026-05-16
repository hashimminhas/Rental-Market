package com.uuriturg.landlord.service;

import com.uuriturg.landlord.domain.LandlordProfile;
import com.uuriturg.landlord.domain.TenantReview;
import com.uuriturg.landlord.dto.TenantReviewRequest;
import com.uuriturg.landlord.dto.TenantReviewResponse;
import com.uuriturg.landlord.exception.DuplicateReviewException;
import com.uuriturg.landlord.exception.LandlordNotFoundException;
import com.uuriturg.landlord.messaging.ReviewEventPublisher;
import com.uuriturg.landlord.repository.ILandlordProfileRepository;
import com.uuriturg.landlord.repository.ITenantReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ITenantReviewRepository reviewRepository;
    private final ILandlordProfileRepository landlordRepository;
    private final ReviewEventPublisher eventPublisher;

    @Override
    public TenantReviewResponse addReview(TenantReviewRequest request) {
        LandlordProfile landlord = landlordRepository.findById(request.getLandlordId())
                .orElseThrow(() -> new LandlordNotFoundException(request.getLandlordId()));

        if (reviewRepository.existsByLandlordIdAndReviewerUserId(request.getLandlordId(), request.getReviewerUserId())) {
            throw new DuplicateReviewException();
        }

        TenantReview saved = reviewRepository.save(TenantReview.builder()
                .landlordId(request.getLandlordId())
                .reviewerUserId(request.getReviewerUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build());

        updateLandlordRating(landlord);
        eventPublisher.publishReviewPosted(saved, landlord);
        log.info("Review {} saved for landlord {} — new avg rating: {}", saved.getReviewId(), landlord.getLandlordId(), landlord.getAverageRating());
        return toResponse(saved);
    }

    @Override
    public List<TenantReviewResponse> getReviewsForLandlord(UUID landlordId) {
        return reviewRepository.findByLandlordId(landlordId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TenantReviewResponse> getReviewsByUser(UUID reviewerUserId) {
        return reviewRepository.findByReviewerUserId(reviewerUserId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private void updateLandlordRating(LandlordProfile landlord) {
        List<TenantReview> allReviews = reviewRepository.findByLandlordId(landlord.getLandlordId());
        double avg = allReviews.stream().mapToInt(TenantReview::getRating).average().orElse(0.0);
        landlord.setAverageRating(Math.round(avg * 10.0) / 10.0);
        landlord.setReviewCount(allReviews.size());
        landlordRepository.save(landlord);
    }

    private TenantReviewResponse toResponse(TenantReview r) {
        return TenantReviewResponse.builder()
                .reviewId(r.getReviewId())
                .landlordId(r.getLandlordId())
                .reviewerUserId(r.getReviewerUserId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
