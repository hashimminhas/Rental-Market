package com.uuriturg.landlord.service;

import com.uuriturg.landlord.dto.CreateLandlordRequest;
import com.uuriturg.landlord.dto.LandlordResponse;
import com.uuriturg.landlord.dto.LandlordSummaryResponse;
import com.uuriturg.landlord.dto.UpdateLandlordRequest;

import java.util.List;
import java.util.UUID;

public interface LandlordService {

    List<LandlordSummaryResponse> getAllLandlords();

    List<LandlordSummaryResponse> getTopRated();

    LandlordResponse getById(UUID landlordId);

    LandlordResponse getByUserId(UUID userId);

    LandlordResponse createLandlord(CreateLandlordRequest request);

    LandlordResponse updateLandlord(UUID landlordId, UpdateLandlordRequest request);

    LandlordResponse verifyLandlord(UUID landlordId);
}
