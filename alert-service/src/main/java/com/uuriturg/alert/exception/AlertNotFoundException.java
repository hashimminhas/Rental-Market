package com.uuriturg.alert.exception;

import java.util.UUID;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(UUID alertId) {
        super("Alert not found with id: " + alertId);
    }
}
