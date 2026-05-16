package com.uuriturg.listing.service;

import com.uuriturg.listing.dto.ClaimListingRequest;
import com.uuriturg.listing.dto.ManagedListingResponse;
import com.uuriturg.listing.dto.ManagedListingSummaryResponse;
import com.uuriturg.listing.dto.UpdateManagedListingRequest;

import java.util.List;
import java.util.UUID;

public interface ManagedListingService {

    ManagedListingResponse claimListing(ClaimListingRequest request);

    List<ManagedListingSummaryResponse> getAllManagedListings();

    ManagedListingResponse getById(UUID managedListingId);

    ManagedListingResponse getByScrapedListingId(UUID scrapedListingId);

    List<ManagedListingResponse> getByLandlord(UUID landlordId);

    List<ManagedListingSummaryResponse> getAvailable(String neighborhood);

    ManagedListingResponse updateListing(UUID managedListingId, UpdateManagedListingRequest request);

    ManagedListingResponse withdrawListing(UUID managedListingId);
}
