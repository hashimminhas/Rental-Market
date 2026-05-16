package com.uuriturg.listing.exception;

import java.util.UUID;

public class ManagedListingNotFoundException extends RuntimeException {

    public ManagedListingNotFoundException(UUID id) {
        super("Managed listing not found: " + id);
    }
}
