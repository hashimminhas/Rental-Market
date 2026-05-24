package com.uuriturg.alert.service;

import com.uuriturg.alert.client.NotificationServiceClient;
import com.uuriturg.alert.client.ScraperServiceClient;
import com.uuriturg.alert.domain.AlertMatch;
import com.uuriturg.alert.domain.AlertRule;
import com.uuriturg.alert.dto.*;
import com.uuriturg.alert.exception.AlertNotFoundException;
import com.uuriturg.alert.repository.IAlertMatchRepository;
import com.uuriturg.alert.repository.IAlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertMatchServiceImpl implements AlertMatchService {

    private final IAlertRuleRepository alertRuleRepository;
    private final IAlertMatchRepository alertMatchRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final ScraperServiceClient scraperServiceClient;

    @Override
    public void evaluateAndMatch(ListingEventDto event) {
        List<AlertRule> activeRules = alertRuleRepository.findByIsActiveTrue();
        log.info("Evaluating {} active alert rules against listing {}", activeRules.size(), event.getListingId());

        for (AlertRule rule : activeRules) {
            if (!matches(rule, event)) continue;

            // skip if already matched this listing for this alert
            if (alertMatchRepository.existsByAlertIdAndListingId(rule.getAlertId(), event.getListingId())) {
                log.debug("Alert {} already matched listing {}, skipping", rule.getAlertId(), event.getListingId());
                continue;
            }

            AlertMatch match = AlertMatch.builder()
                    .alertId(rule.getAlertId())
                    .listingId(event.getListingId())
                    .notified(false)
                    .build();
            AlertMatch saved = alertMatchRepository.save(match);

            boolean notified = sendNotification(rule, event);

            if (notified) {
                saved.setNotified(true);
                alertMatchRepository.save(saved);
            }

            log.info("Alert {} matched listing {} — notified={}", rule.getAlertId(), event.getListingId(), notified);
        }
    }

    @Override
    public List<AlertMatchResponse> getMatchesForAlert(UUID alertId) {
        if (!alertRuleRepository.existsById(alertId)) {
            throw new AlertNotFoundException(alertId);
        }
        return alertMatchRepository.findByAlertId(alertId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void scanExistingListings(UUID alertId) {
        AlertRule rule = alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new com.uuriturg.alert.exception.AlertNotFoundException(alertId));

        List<ListingEventDto> listings = scraperServiceClient.findMatchingListings(
                rule.getNeighborhood(), rule.getMaxPrice(), rule.getMinSize());

        log.info("Scanning {} existing listings for new alert {}", listings.size(), alertId);

        for (ListingEventDto listing : listings) {
            if (!matches(rule, listing)) continue;
            if (alertMatchRepository.existsByAlertIdAndListingId(rule.getAlertId(), listing.getListingId())) continue;

            AlertMatch match = AlertMatch.builder()
                    .alertId(rule.getAlertId())
                    .listingId(listing.getListingId())
                    .notified(false)
                    .build();
            AlertMatch saved = alertMatchRepository.save(match);

            boolean notified = sendNotification(rule, listing);
            if (notified) {
                saved.setNotified(true);
                alertMatchRepository.save(saved);
            }
            log.info("Existing listing {} matched alert {} — notified={}", listing.getListingId(), alertId, notified);
        }
    }

    @Override
    public void testFire(UUID alertId) {
        AlertRule rule = alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new com.uuriturg.alert.exception.AlertNotFoundException(alertId));

        ListingEventDto fakeEvent = ListingEventDto.builder()
                .listingId(UUID.randomUUID())
                .title("Test listing — " + (rule.getNeighborhood() != null ? rule.getNeighborhood() : "Tartu"))
                .neighborhood(rule.getNeighborhood() != null ? rule.getNeighborhood() : "Tartu")
                .price(rule.getMaxPrice() != null ? rule.getMaxPrice() : java.math.BigDecimal.valueOf(500))
                .size(rule.getMinSize() != null ? rule.getMinSize() : java.math.BigDecimal.valueOf(45))
                .rooms(rule.getMinRooms() != null ? rule.getMinRooms() : 2)
                .url("http://localhost:5173/listings")
                .build();

        log.info("Test-firing alert {} to {}", alertId, rule.getEmail());
        sendNotification(rule, fakeEvent);
    }

    private boolean matches(AlertRule rule, ListingEventDto listing) {
        if (rule.getNeighborhood() != null && listing.getNeighborhood() != null &&
                !rule.getNeighborhood().equalsIgnoreCase(listing.getNeighborhood())) return false;

        if (rule.getMinPrice() != null && listing.getPrice() != null &&
                listing.getPrice().compareTo(rule.getMinPrice()) < 0) return false;

        if (rule.getMaxPrice() != null && listing.getPrice() != null &&
                listing.getPrice().compareTo(rule.getMaxPrice()) > 0) return false;

        if (rule.getMinSize() != null && listing.getSize() != null &&
                listing.getSize().compareTo(rule.getMinSize()) < 0) return false;

        if (rule.getMinRooms() != null && listing.getRooms() != null &&
                listing.getRooms() < rule.getMinRooms()) return false;

        return true;
    }

    private boolean sendNotification(AlertRule rule, ListingEventDto listing) {
        try {
            String alertName = rule.getName() != null ? rule.getName() : "your alert";
            String subject = "New listing matches \"" + alertName + "\" - " +
                    (listing.getNeighborhood() != null ? listing.getNeighborhood() : "Tartu");
            String body = String.format(
                    "Hi!\n\nA new listing matches your alert \"%s\".\n\nTitle: %s\nPrice: EUR %.2f/month\nNeighborhood: %s\nSize: %s m2\nURL: %s\n\nUuriturg",
                    alertName,
                    listing.getTitle() != null ? listing.getTitle() : "Apartment",
                    listing.getPrice() != null ? listing.getPrice() : 0,
                    listing.getNeighborhood() != null ? listing.getNeighborhood() : "Unknown",
                    listing.getSize() != null ? listing.getSize().toPlainString() : "?",
                    listing.getUrl() != null ? listing.getUrl() : ""
            );

            notificationServiceClient.sendNotification(NotificationRequest.builder()
                    .recipientEmail(rule.getEmail())
                    .channel("EMAIL")
                    .subject(subject)
                    .body(body)
                    .build());
            return true;
        } catch (Exception e) {
            log.warn("Failed to send notification for alert {}: {}", rule.getAlertId(), e.getMessage());
            return false;
        }
    }

    private AlertMatchResponse toResponse(AlertMatch match) {
        return AlertMatchResponse.builder()
                .matchId(match.getMatchId())
                .alertId(match.getAlertId())
                .listingId(match.getListingId())
                .matchedAt(match.getMatchedAt())
                .notified(match.getNotified())
                .build();
    }
}
