package com.uuriturg.notification.repository;

import com.uuriturg.notification.domain.Notification;
import com.uuriturg.notification.domain.NotificationChannel;
import com.uuriturg.notification.domain.NotificationStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface INotificationRepository extends CrudRepository<Notification, UUID> {

    List<Notification> findByRecipientUserId(UUID recipientUserId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipientUserIdAndChannel(UUID recipientUserId, NotificationChannel channel);
}
