package com.berss.platform.reports.domain.model.commands;

public record CreateDailySummaryCommand(
        Long employeeId,
        Long companyId,
        Integer day,
        Integer month,
        Integer year,
        Double entryTime,
        Double exitTime,
        Double inputAmount,
        Integer score
) {

    public CreateDailySummaryCommand {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("employee id cannot be null or less than 1");
        }

        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("company id cannot be null or less than 1");
        }

        if (day == null || day <= 0 || day > 31) {
            throw new IllegalArgumentException("day cannot be null, less than 1 or greater than 31");
        }

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("month cannot be null, less than 1 or greater than 12");
        }

        if (year == null || year <= 2000 || year >= 2100) {
            throw new IllegalArgumentException("year cannot be null, less than 2000 or greater than 2100");
        }

        if (entryTime == null || entryTime < 0 || entryTime > 24) {
            throw new IllegalArgumentException("entryTime cannot be null, less than 0 or greater than 24");
        }

        if (exitTime == null || exitTime < 0 || exitTime > 24) {
            throw new IllegalArgumentException("exitTime cannot be null, less than 0 or greater than 24");
        }

        if (exitTime <= entryTime) {
            throw new IllegalArgumentException("exitTime must be greater than entryTime");
        }

        if (score == null || score < 0 || score > 10) {
            throw new IllegalArgumentException("score cannot be null, less than 0 or greater than 10");
        }
    }
}
