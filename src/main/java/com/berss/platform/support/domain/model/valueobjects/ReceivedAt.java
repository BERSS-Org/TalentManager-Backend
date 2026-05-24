package com.berss.platform.support.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Embeddable
public record ReceivedAt(LocalDateTime value) {
    public ReceivedAt {
        if (value == null)
            throw new IllegalArgumentException("ReceivedAt cannot be null");

        // The client sends its local wall-clock time as a zone-less LocalDateTime.
        // Allow up to a day of skew so any timezone's "now" is accepted while still
        // rejecting clearly invalid future dates.
        var valueUtc = value.atOffset(ZoneOffset.UTC).toInstant();
        var limitUtc = Instant.now().plus(Duration.ofDays(1));

        if (valueUtc.isAfter(limitUtc))
            throw new IllegalArgumentException("ReceivedAt cannot be in the future");
    }
}
