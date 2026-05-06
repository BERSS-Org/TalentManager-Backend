package com.berss.platform.reports.interfaces.rest.resources;

/**
 * Computed view of an employee's monthly performance.
 * Built on the fly from {@code DailySummary} records — never persisted.
 *
 * Cost is derived from each employee's {@code hourlyCost} (or company default) and worked hours.
 * Margin = totalInput - totalCost.
 */
public record MonthlyAggregateResource(
        Long employeeId,
        String employeeName,
        String teamName,
        int year,
        int month,
        int dayCount,
        double completedHours,
        int totalHours,
        double completionRate,
        double totalInput,
        double totalCost,
        double margin,
        double averageScore
) {}
