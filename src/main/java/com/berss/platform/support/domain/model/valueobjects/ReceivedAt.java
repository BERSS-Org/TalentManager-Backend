package com.berss.platform.support.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;

/**
 * Instant a support message was received. Stored as a UTC instant so timestamps
 * are unambiguous across timezones; the client sends an ISO-8601 instant.
 */
@Embeddable
public record ReceivedAt(Instant value) {
    public ReceivedAt {
        if (value == null)
            throw new IllegalArgumentException("ReceivedAt cannot be null");

        // Reject only clearly-future instants; a few minutes of clock skew is fine.
        if (value.isAfter(Instant.now().plus(Duration.ofMinutes(5))))
            throw new IllegalArgumentException("ReceivedAt cannot be in the future");
    }
}
