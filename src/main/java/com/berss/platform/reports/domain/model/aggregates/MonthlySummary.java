package com.berss.platform.reports.domain.model.aggregates;

import com.berss.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import jakarta.persistence.*;

// Value Objects

import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.reports.domain.model.valueobjects.YearMonthPeriod;
import com.berss.platform.reports.domain.model.valueobjects.Score;
import com.berss.platform.reports.domain.model.valueobjects.InputAmount;

// Commands
import com.berss.platform.reports.domain.model.commands.CreateMonthlySummaryCommand;
import com.berss.platform.reports.domain.model.commands.UpdateMonthlySummaryCommand;

/**
 * Monthly Summary aggregate root
 */
@Entity
public class MonthlySummary extends AuditableAbstractAggregateRoot<MonthlySummary> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "employee_id"))
    private EmployeeId employeeId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    private CompanyId companyId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "year", column = @Column(name = "summary_year")),
            @AttributeOverride(name = "month", column = @Column(name = "summary_month"))
    })
    private YearMonthPeriod period;

    private Integer totalHours;

    private Integer completedHours;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "score"))
    private Score score;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "input_amount"))
    private InputAmount inputAmount;

    /**
     * Default constructor
     */
    public MonthlySummary() {}

    /**
     * Constructor with all fields
     */
    public MonthlySummary(Long employeeId, Long companyId, int _year, int _month, int totalHours, int completedHours, double _inputAmount, int _score) {
        this.employeeId = new EmployeeId(employeeId);
        this.companyId = new CompanyId(companyId);
        this.period = new YearMonthPeriod(_year, _month);
        this.totalHours = totalHours;
        this.completedHours = completedHours;
        this.inputAmount = new InputAmount(_inputAmount);
        this.score = new Score(_score);
    }

    /**
     * Constructor with CreateMonthlySummaryCommand
     */
    public MonthlySummary(CreateMonthlySummaryCommand command) {
        this.employeeId = new EmployeeId(command.employeeId());
        this.companyId = new CompanyId(command.companyId());
        this.period = new YearMonthPeriod(command.year(), command.month());
        this.totalHours = command.totalHours();
        this.completedHours = command.completedHours();
        this.inputAmount = new InputAmount(command.inputAmount());
        this.score = new Score(command.score());
    }

    // Getters

    public double getInput() {
        return inputAmount.getValue();
    }

    public int getScore() {
        return score.getValue();
    }

    public long getEmployeeId() {
        return employeeId.getValue();
    }

    public long getCompanyId() {
        return companyId.getValue();
    }

    public YearMonthPeriod getPeriod() {
        return period;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getCompletedHours() {
        return completedHours;
    }

    // Setters / Updaters

    public void updateTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public void updateCompletedHours(int completedHours) {
        this.completedHours = completedHours;
    }

    public void updateScore(int value) {
        this.score = new Score(value);
    }

    public void updateInput(double value) {
        this.inputAmount = new InputAmount(value);
    }

    public void updateMonthlySummary(UpdateMonthlySummaryCommand command) {
        this.totalHours = command.totalHours();
        this.completedHours = command.completedHours();
        this.score = new Score(command.score());
        this.inputAmount = new InputAmount(command.inputAmount());
    }
}
