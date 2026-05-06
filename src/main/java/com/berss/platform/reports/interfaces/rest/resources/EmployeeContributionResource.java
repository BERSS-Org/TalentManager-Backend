package com.berss.platform.reports.interfaces.rest.resources;

/**
 * Per-employee revenue / cost / margin contribution over a window.
 * Used by the Dashboard "Top contributors" chart.
 */
public record EmployeeContributionResource(
        Long employeeId,
        String employeeName,
        String teamName,
        double hoursWorked,
        double totalInput,
        double totalCost,
        double margin,
        double marginRate
) {}
