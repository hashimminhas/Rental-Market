package com.uuriturg.landlord.service;

import com.uuriturg.landlord.client.UserServiceClient;
import com.uuriturg.landlord.domain.LandlordProfile;
import com.uuriturg.landlord.dto.*;
import com.uuriturg.landlord.exception.LandlordNotFoundException;
import com.uuriturg.landlord.repository.ILandlordProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandlordServiceImpl implements LandlordService {

    private final ILandlordProfileRepository landlordRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public List<LandlordSummaryResponse> getAllLandlords() {
        return StreamSupport.stream(landlordRepository.findAll().spliterator(), false)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<LandlordSummaryResponse> getTopRated() {
        return landlordRepository.findTop10ByOrderByAverageRatingDesc()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public LandlordResponse getById(UUID landlordId) {
        LandlordProfile profile = landlordRepository.findById(landlordId)
                .orElseThrow(() -> new LandlordNotFoundException(landlordId));
        return toResponse(profile);
    }

    @Override
    public LandlordResponse getByUserId(UUID userId) {
        LandlordProfile profile = landlordRepository.findByUserId(userId)
                .orElseThrow(() -> new LandlordNotFoundException(userId));
        return toResponse(profile);
    }

    @Override
    public LandlordResponse createLandlord(CreateLandlordRequest request) {
        ValidateUserDto user = userServiceClient.validateUser(request.getUserId());
        if (user == null || !Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("User not found or inactive: " + request.getUserId());
        }
        LandlordProfile saved = landlordRepository.save(LandlordProfile.builder()
                .userId(request.getUserId())
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .phoneNumber(request.getPhoneNumber())
                .build());
        log.info("Created landlord profile {} for user {}", saved.getLandlordId(), saved.getUserId());
        return toResponse(saved);
    }

    @Override
    public LandlordResponse updateLandlord(UUID landlordId, UpdateLandlordRequest request) {
        LandlordProfile profile = landlordRepository.findById(landlordId)
                .orElseThrow(() -> new LandlordNotFoundException(landlordId));
        if (request.getDisplayName() != null) profile.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        return toResponse(landlordRepository.save(profile));
    }

    @Override
    public LandlordResponse verifyLandlord(UUID landlordId) {
        LandlordProfile profile = landlordRepository.findById(landlordId)
                .orElseThrow(() -> new LandlordNotFoundException(landlordId));
        profile.setIsVerified(true);
        return toResponse(landlordRepository.save(profile));
    }

    private LandlordSummaryResponse toSummary(LandlordProfile p) {
        return LandlordSummaryResponse.builder()
                .landlordId(p.getLandlordId())
                .displayName(p.getDisplayName())
                .isVerified(p.getIsVerified())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .build();
    }

    private LandlordResponse toResponse(LandlordProfile p) {
        return LandlordResponse.builder()
                .landlordId(p.getLandlordId())
                .userId(p.getUserId())
                .displayName(p.getDisplayName())
                .bio(p.getBio())
                .phoneNumber(p.getPhoneNumber())
                .isVerified(p.getIsVerified())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
