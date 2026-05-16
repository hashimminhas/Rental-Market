package com.uuriturg.analytics.exception;

public class SnapshotNotFoundException extends RuntimeException {

    public SnapshotNotFoundException(String neighborhood) {
        super("No analytics data found for neighborhood: " + neighborhood);
    }
}
