package com.berss.platform.iam.interfaces.rest.resources;

public record ChangePasswordResource(
        String currentPassword,
        String newPassword
) {
    public ChangePasswordResource {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must have at least 8 characters");
        }
    }
}
