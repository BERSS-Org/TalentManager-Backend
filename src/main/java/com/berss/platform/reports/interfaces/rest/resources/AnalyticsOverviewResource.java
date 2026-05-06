package com.berss.platform.reports.interfaces.rest.resources;

import java.util.List;

public record AnalyticsOverviewResource(
        Long companyId,
        int totalEmployees,
        int totalTeams,
        double totalInput,
        double averageScore,
        int totalHours,
        int completedHours,
        double completionRate,
        int openSupportMessages,
        String healthLabel,
        List<String> insights
) {
}
