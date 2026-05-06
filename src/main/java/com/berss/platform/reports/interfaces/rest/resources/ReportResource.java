package com.berss.platform.reports.interfaces.rest.resources;

public record ReportResource(
        Long id,
        String title,
        String content,
        Long companyId,
        Long employeeId,
        Integer year,
        Integer month
) {}
