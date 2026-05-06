package com.berss.platform.reports.domain.model.commands;

/**
 * Command to update a daily summary
 *
 * @param entryTime the daily summary entry hour for an employee (decimal, e.g. 9.5 for 09:30).
 * @param exitTime the daily summary exit hour for an employee (decimal, e.g. 17.5 for 17:30).
 * @param score the daily summary score for an employee. Range 0..10.
 * @param inputAmount the daily summary inputAmount for an employee. Cannot be negative.
 */

public record UpdateDailySummaryCommand(Long dailySummaryId, Double entryTime, Double exitTime, Integer score, Double inputAmount) {
    public UpdateDailySummaryCommand {
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

        if (inputAmount != null && inputAmount < 0) {
            throw new IllegalArgumentException("inputAmount cannot be negative");
        }
    }
}
