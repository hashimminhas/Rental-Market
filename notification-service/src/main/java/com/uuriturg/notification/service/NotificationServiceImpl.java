package com.uuriturg.notification.service;

import com.uuriturg.notification.domain.Notification;
import com.uuriturg.notification.domain.NotificationChannel;
import com.uuriturg.notification.domain.NotificationStatus;
import com.uuriturg.notification.dto.NotificationResponse;
import com.uuriturg.notification.dto.SendNotificationRequest;
import com.uuriturg.notification.exception.NotificationNotFoundException;
import com.uuriturg.notification.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final INotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public NotificationResponse send(SendNotificationRequest request) {
        NotificationChannel channel;
        try {
            channel = NotificationChannel.valueOf(request.getChannel().toUpperCase());
        } catch (IllegalArgumentException e) {
            channel = NotificationChannel.EMAIL;
        }

        String recipientEmail = request.getRecipientEmail();

        Notification notification = notificationRepository.save(Notification.builder()
                .recipientUserId(request.getRecipientUserId())
                .channel(channel)
                .subject(request.getSubject())
                .body(request.getBody())
                .recipientEmail(recipientEmail)
                .build());

        if (channel == NotificationChannel.EMAIL && recipientEmail != null) {
            boolean sent = sendEmail(recipientEmail, request.getSubject(), request.getBody());
            notification.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
            if (sent) notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        }

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse getById(UUID notificationId) {
        return toResponse(notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId)));
    }

    @Override
    public List<NotificationResponse> getByRecipient(UUID recipientUserId) {
        return notificationRepository.findByRecipientUserId(recipientUserId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getPending() {
        return notificationRepository.findByStatus(NotificationStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public NotificationResponse retry(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (notification.getChannel() == NotificationChannel.EMAIL && notification.getRecipientEmail() != null) {
            boolean sent = sendEmail(notification.getRecipientEmail(), notification.getSubject(), notification.getBody());
            notification.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
            if (sent) notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        }

        return toResponse(notificationRepository.save(notification));
    }

    private boolean sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured — skipping email to {}", to);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Üüriturg Alerts <works.hashiim@gmail.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .recipientUserId(n.getRecipientUserId())
                .recipientEmail(n.getRecipientEmail())
                .channel(n.getChannel())
                .subject(n.getSubject())
                .body(n.getBody())
                .status(n.getStatus())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
