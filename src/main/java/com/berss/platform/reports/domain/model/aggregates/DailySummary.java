package com.berss.platform.reports.domain.model.aggregates;

import com.berss.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import jakarta.persistence.*;

// Value Objects

import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.reports.domain.model.valueobjects.Score;
import com.berss.platform.reports.domain.model.valueobjects.InputAmount;
import com.berss.platform.shared.domain.model.valueobjects.SpecificDate;

// Commands
import com.berss.platform.reports.domain.model.commands.CreateDailySummaryCommand;
import com.berss.platform.reports.domain.model.commands.UpdateDailySummaryCommand;

import java.time.LocalDate;

/**
 * Daily Summary aggregate root.
 * entryTime / exitTime are decimal hours of the day (e.g. 9.5 for 09:30).
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_daily_summary_employee_company_date",
                columnNames = {"employee_id", "company_id", "summary_day", "summary_month", "summary_year"}
        )
})
public class DailySummary extends AuditableAbstractAggregateRoot<DailySummary> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "employee_id"))
    private EmployeeId employeeId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    private CompanyId companyId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "day", column = @Column(name = "summary_day")),
            @AttributeOverride(name = "month", column = @Column(name = "summary_month")),
            @AttributeOverride(name = "year", column = @Column(name = "summary_year"))
    })
    private SpecificDate specificDate;

    private Double entryTime;

    private Double exitTime;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "score"))
    private Score score;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "input_amount"))
    private InputAmount inputAmount;

    /**
     * Default constructor
     */
    public DailySummary() {}

    /**
     * Constructor with all fields
     */
    public DailySummary(Long employeeId, Long companyId, int _day, int _month, int _year, double _entryTime, double _exitTime, double _inputAmount, int _score) {
        this.employeeId = new EmployeeId(employeeId);
        this.companyId = new CompanyId(companyId);
        this.specificDate = new SpecificDate(_day, _month, _year);
        this.entryTime = _entryTime;
        this.exitTime = _exitTime;
        this.inputAmount = new InputAmount(_inputAmount);
        this.score = new Score(_score);
    }

    /**
     * Constructor with CreateDailySummaryCommand
     */
    public DailySummary(CreateDailySummaryCommand command) {
        this.employeeId = new EmployeeId(command.employeeId());
        this.companyId = new CompanyId(command.companyId());
        this.specificDate = new SpecificDate(command.day(), command.month(), command.year());
        this.entryTime = command.entryTime();
        this.exitTime = command.exitTime();
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

    public InputAmount getInputAmount() { return inputAmount; }

    public Integer getDay() { return specificDate.day(); }

    public Integer getMonth() { return specificDate.month(); }

    public Integer getYear() { return specificDate.year(); }

    public LocalDate getLocalDate() { return specificDate.toLocalDate(); }

    public Double getEntryTime() { return entryTime; }

    public Double getExitTime() { return exitTime; }

    /**
     * Hours worked on this day (exit minus entry).
     */
    public double getHoursWorked() {
        if (entryTime == null || exitTime == null) return 0;
        return Math.max(0, exitTime - entryTime);
    }

    // Setters / Updaters

    public void updateEntryTime(double entryTime) {
        this.entryTime = entryTime;
    }

    public void updateExitTime(double exitTime) {
        this.exitTime = exitTime;
    }

    public void updateScore(int value) {
        this.score = new Score(value);
    }

    public void updateInput(double value) {
        this.inputAmount = new InputAmount(value);
    }

    /**
     * Updater with UpdateDailySummaryCommand
     */
    public void updateDailySummary(UpdateDailySummaryCommand command) {
        this.entryTime = command.entryTime();
        this.exitTime = command.exitTime();
        this.score = new Score(command.score());
        this.inputAmount = new InputAmount(command.inputAmount());
    }
}
