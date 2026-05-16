package com.uuriturg.listing.exception;

import java.util.UUID;

public class ListingAlreadyClaimedException extends RuntimeException {

    public ListingAlreadyClaimedException(UUID scrapedListingId) {
        super("Listing already claimed by a landlord: " + scrapedListingId);
    }
}
