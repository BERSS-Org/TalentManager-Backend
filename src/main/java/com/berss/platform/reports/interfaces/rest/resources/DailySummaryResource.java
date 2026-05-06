package com.berss.platform.reports.interfaces.rest.resources;

public record DailySummaryResource(
        Long id,
        Long employeeId,
        Long companyId,
        Integer day,
        Integer month,
        Integer year,
        Double entryTime,
        Double exitTime,
        Double hoursWorked,
        Double inputAmount,
        Integer score
) {}
