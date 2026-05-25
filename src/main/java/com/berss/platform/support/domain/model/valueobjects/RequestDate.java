package com.berss.platform.support.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;

/**
 * Instant a support request was raised. Stored as a UTC instant so timestamps
 * are unambiguous across timezones; cannot be null or clearly in the future.
 */
@Embeddable
public record RequestDate(Instant value) {
    public RequestDate {
        if (value == null) {
            throw new IllegalArgumentException("Request date cannot be null");
        }

        // Reject only clearly-future instants; a few minutes of clock skew is fine.
        if (value.isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            throw new IllegalArgumentException("Request date cannot be in the future");
        }
    }
}
