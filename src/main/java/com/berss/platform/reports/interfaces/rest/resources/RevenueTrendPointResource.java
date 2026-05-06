package com.berss.platform.reports.interfaces.rest.resources;

/**
 * One point in the company-wide 6-month trend.
 */
public record RevenueTrendPointResource(
        int year,
        int month,
        String label,
        double totalInput,
        double totalHours
) {}
