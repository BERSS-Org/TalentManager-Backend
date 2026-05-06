package com.berss.platform.reports.interfaces.rest.resources;

public record CreateReportResource(
        String title,
        String content,
        Long companyId,
        Long employeeId,
        Integer year,
        Integer month
) {
    public CreateReportResource {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID cannot be null or less than 1");
        }
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID cannot be null or less than 1");
        }
        if (year == null || year <= 2000 || year >= 2100) {
            throw new IllegalArgumentException("Year must be between 2001 and 2099");
        }
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
    }
}
