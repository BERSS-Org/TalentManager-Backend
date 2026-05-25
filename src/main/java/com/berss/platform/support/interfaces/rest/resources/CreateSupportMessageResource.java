package com.berss.platform.support.interfaces.rest.resources;

import java.time.Instant;

public record CreateSupportMessageResource(String content, Long companyId, Instant requestDate, Instant receivedAt) {
    public CreateSupportMessageResource {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        if (companyId == null || companyId < 1) {
            throw new IllegalArgumentException("Company ID is required and must be greater than 0");
        }
        if (requestDate == null || receivedAt == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
    }
}