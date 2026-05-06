package com.berss.platform.business.interfaces.rest.resources;

/**
 * Read/write payload for company-wide operating defaults.
 * The combination of expectedDailyHours * workingDaysPerMonth defines
 * the monthly cumplimiento target used across reports and analytics.
 */
public record CompanySettingsResource(
        Long companyId,
        String name,
        String currencyCode,
        Double expectedDailyHours,
        Integer workingDaysPerMonth,
        Double defaultHourlyRate,
        Double defaultHourlyCost
) {}
