package com.uuriturg.landlord.exception;

import java.util.UUID;

public class LandlordNotFoundException extends RuntimeException {

    public LandlordNotFoundException(UUID landlordId) {
        super("Landlord not found: " + landlordId);
    }
}
