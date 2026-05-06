package com.berss.platform.business.domain.model.entities;

import com.berss.platform.business.domain.model.valueobjects.CompanyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String email;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    /** Expected hours of work per day per employee — drives the cumplimiento target. */
    @Column(name = "expected_daily_hours")
    private Double expectedDailyHours;

    /** Working days the company plans for in a regular month. */
    @Column(name = "working_days_per_month")
    private Integer workingDaysPerMonth;

    /** ISO 4217 code (e.g. USD, EUR, PEN) used to format money throughout the app. */
    @Column(name = "currency_code", length = 8)
    private String currencyCode;

    /** Default per-hour billing rate applied when an employee has no individual hourlyRate. */
    @Column(name = "default_hourly_rate")
    private Double defaultHourlyRate;

    /** Default per-hour cost applied when an employee has no individual hourlyCost. */
    @Column(name = "default_hourly_cost")
    private Double defaultHourlyCost;

    public Company() {
    }

    public Company(String name, String description, String email, CompanyStatus status) {
        this.name = name;
        this.description = description;
        this.email = email;
        this.status = status;
        this.expectedDailyHours = 8.0;
        this.workingDaysPerMonth = 21;
        this.currencyCode = "USD";
        this.defaultHourlyRate = 0.0;
        this.defaultHourlyCost = 0.0;
    }

    public double getExpectedDailyHoursOrDefault() {
        return expectedDailyHours == null || expectedDailyHours <= 0 ? 8.0 : expectedDailyHours;
    }

    public int getWorkingDaysPerMonthOrDefault() {
        return workingDaysPerMonth == null || workingDaysPerMonth <= 0 ? 21 : workingDaysPerMonth;
    }

    public String getCurrencyCodeOrDefault() {
        return currencyCode == null || currencyCode.isBlank() ? "USD" : currencyCode;
    }

    public double getDefaultHourlyRateOrZero() {
        return defaultHourlyRate == null ? 0.0 : Math.max(0.0, defaultHourlyRate);
    }

    public double getDefaultHourlyCostOrZero() {
        return defaultHourlyCost == null ? 0.0 : Math.max(0.0, defaultHourlyCost);
    }
}
