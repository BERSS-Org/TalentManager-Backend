package com.berss.platform.reports.interfaces.rest.resources;

public record TeamPerformanceResource(
        String teamName,
        int employeeCount,
        double totalInput,
        double averageScore,
        int completedHours,
        int totalHours,
        double completionRate
) {
}
