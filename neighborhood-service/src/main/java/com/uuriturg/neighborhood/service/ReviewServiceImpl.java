package com.uuriturg.neighborhood.service;

import com.uuriturg.neighborhood.domain.Review;
import com.uuriturg.neighborhood.dto.ReviewRequest;
import com.uuriturg.neighborhood.dto.ReviewResponse;
import com.uuriturg.neighborhood.exception.DuplicateReviewException;
import com.uuriturg.neighborhood.exception.NeighborhoodNotFoundException;
import com.uuriturg.neighborhood.repository.INeighborhoodRepository;
import com.uuriturg.neighborhood.repository.IReviewRepository;
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

    private final IReviewRepository reviewRepository;
    private final INeighborhoodRepository neighborhoodRepository;

    @Override
    public ReviewResponse addReview(ReviewRequest request) {
        if (!neighborhoodRepository.existsById(request.getNeighborhoodId())) {
            throw new NeighborhoodNotFoundException(request.getNeighborhoodId().toString());
        }
        if (reviewRepository.existsByNeighborhoodIdAndUserId(request.getNeighborhoodId(), request.getUserId())) {
            throw new DuplicateReviewException();
        }
        Review saved = reviewRepository.save(Review.builder()
                .neighborhoodId(request.getNeighborhoodId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build());
        log.info("Review {} saved for neighborhood {} by user {}", saved.getReviewId(), saved.getNeighborhoodId(), saved.getUserId());
        return toResponse(saved);
    }

    @Override
    public List<ReviewResponse> getReviewsForNeighborhood(UUID neighborhoodId) {
        return reviewRepository.findByNeighborhoodId(neighborhoodId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(UUID userId) {
        return reviewRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .neighborhoodId(r.getNeighborhoodId())
                .userId(r.getUserId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
