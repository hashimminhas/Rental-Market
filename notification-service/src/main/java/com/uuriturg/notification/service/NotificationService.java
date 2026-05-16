package com.uuriturg.notification.service;

import com.uuriturg.notification.dto.*;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse send(SendNotificationRequest request);

    NotificationResponse getById(UUID notificationId);

    List<NotificationResponse> getByRecipient(UUID recipientUserId);

    List<NotificationResponse> getPending();

    NotificationResponse retry(UUID notificationId);

    void sendFromListingEvent(ListingClaimedEventDto event);

    void sendFromReviewEvent(ReviewPostedEventDto event);
}
