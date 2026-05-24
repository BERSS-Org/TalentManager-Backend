package com.berss.platform.reports.application.internal.eventhandlers;

import com.berss.platform.employees.domain.model.events.EmployeeDeletedEvent;
import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.DailySummaryRepository;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.ReportRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Keeps the reports context consistent when an employee is deleted: removes the
 * daily summaries and reports anchored to that employee so they no longer linger
 * as orphans (e.g. showing "Employee #N" in the report history).
 */
@Component
public class EmployeeDeletedEventHandler {

    private final DailySummaryRepository dailySummaryRepository;
    private final ReportRepository reportRepository;

    public EmployeeDeletedEventHandler(DailySummaryRepository dailySummaryRepository,
                                       ReportRepository reportRepository) {
        this.dailySummaryRepository = dailySummaryRepository;
        this.reportRepository = reportRepository;
    }

    @EventListener
    public void on(EmployeeDeletedEvent event) {
        var dailies = dailySummaryRepository.findDailySummariesByEmployeeId(new EmployeeId(event.employeeId()));
        if (!dailies.isEmpty()) {
            dailySummaryRepository.deleteAll(dailies);
        }

        var reports = reportRepository.findByEmployeeId(event.employeeId());
        if (!reports.isEmpty()) {
            reportRepository.deleteAll(reports);
        }
    }
}
