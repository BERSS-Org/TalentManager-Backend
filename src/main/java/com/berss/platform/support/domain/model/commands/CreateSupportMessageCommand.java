package com.berss.platform.support.domain.model.commands;

import java.time.Instant;

public record CreateSupportMessageCommand(String content, Long companyId, Instant requestDate, Instant receivedAt) {
    public CreateSupportMessageCommand {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }

    }
}
