package com.berss.platform.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String username, Long managerId, Long companyId, String token) {
}
