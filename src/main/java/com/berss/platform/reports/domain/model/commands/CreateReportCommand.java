package com.berss.platform.reports.domain.model.commands;

/**
 * Create a Report rooted in a (employee, year, month) computed aggregate.
 * No persisted MonthlySummary is required — Reports point to a period directly.
 */
public record CreateReportCommand(
        String title,
        String content,
        Long companyId,
        Long employeeId,
        Integer year,
        Integer month
) {
    public CreateReportCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }

        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("companyId cannot be null or less than 1");
        }

        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("employeeId cannot be null or less than 1");
        }

        if (year == null || year <= 2000 || year >= 2100) {
            throw new IllegalArgumentException("year must be between 2001 and 2099");
        }

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
    }
}
