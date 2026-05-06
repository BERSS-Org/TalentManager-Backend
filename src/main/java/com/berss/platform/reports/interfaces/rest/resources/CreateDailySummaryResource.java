package com.berss.platform.reports.interfaces.rest.resources;

public record CreateDailySummaryResource(
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

    public CreateDailySummaryResource {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID cannot be null or less than 1");
        }

        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID cannot be null or less than 1");
        }

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Month cannot be null, less than 1 or greater than 12");
        }

        if (year == null || year <= 2000 || year >= 2100) {
            throw new IllegalArgumentException("Year cannot be null, less than 2000 or greater than 2100");
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

        if (inputAmount != null && inputAmount < 0) {
            throw new IllegalArgumentException("InputAmount cannot be negative");
        }

        if (score == null || score < 0 || score > 10) {
            throw new IllegalArgumentException("Score cannot be null, less than 0 or greater than 10");
        }
    }
}
