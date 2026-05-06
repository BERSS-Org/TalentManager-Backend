package com.berss.platform.reports.domain.model.aggregates;

import com.berss.platform.reports.domain.model.commands.UpdateReportCommand;
import com.berss.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;

import jakarta.persistence.*;

import com.berss.platform.reports.domain.model.commands.CreateReportCommand;
import lombok.Getter;

/**
 * Report aggregate.
 * Anchored to a (employee, year, month) computed monthly aggregate
 * — no persisted MonthlySummary is required anymore.
 */
@Entity
public class Report extends AuditableAbstractAggregateRoot<Report> {

    @Getter
    private String title;

    @Getter
    @Column(columnDefinition = "TEXT")
    private String content;

    @Embedded
    @AttributeOverride(name = "companyId", column = @Column(name = "company_id"))
    private CompanyId companyId;

    @Getter
    private Long employeeId;

    @Getter
    @Column(name = "period_year")
    private Integer year;

    @Getter
    @Column(name = "period_month")
    private Integer month;

    // Constructors

    public Report() {}

    public Report(String title, String content, Long companyId, Long employeeId, Integer year, Integer month) {
        this.title = title;
        this.content = content;
        this.companyId = new CompanyId(companyId);
        this.employeeId = employeeId;
        this.year = year;
        this.month = month;
    }

    public Report(CreateReportCommand command) {
        this.title = command.title();
        this.content = command.content();
        this.companyId = new CompanyId(command.companyId());
        this.employeeId = command.employeeId();
        this.year = command.year();
        this.month = command.month();
    }

    public Long getCompanyId() {
        return companyId.getValue();
    }

    // Updaters

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateReport(UpdateReportCommand command) {
        this.title = command.title();
        this.content = command.content();
    }
}
