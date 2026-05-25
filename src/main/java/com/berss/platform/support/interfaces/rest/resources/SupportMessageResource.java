package com.berss.platform.support.interfaces.rest.resources;

import java.time.Instant;

public record SupportMessageResource(Long id, String content, Long companyId, Instant requestDate, Instant receivedAt, String status) {
}