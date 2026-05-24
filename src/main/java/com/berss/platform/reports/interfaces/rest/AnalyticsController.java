package com.berss.platform.reports.interfaces.rest;

import com.berss.platform.business.domain.model.entities.Company;
import com.berss.platform.business.infrastructure.persistence.jpa.repositories.CompanyQueryRepository;
import com.berss.platform.employees.domain.model.aggregates.Employee;
import com.berss.platform.employees.infrastructure.persistence.jpa.repositories.EmployeeRepository;
import com.berss.platform.reports.domain.model.aggregates.DailySummary;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.DailySummaryRepository;
import com.berss.platform.reports.interfaces.rest.resources.AnalyticsOverviewResource;
import com.berss.platform.reports.interfaces.rest.resources.EmployeeContributionResource;
import com.berss.platform.reports.interfaces.rest.resources.MonthlyAggregateResource;
import com.berss.platform.reports.interfaces.rest.resources.RevenueTrendPointResource;
import com.berss.platform.reports.interfaces.rest.resources.TeamPerformanceResource;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import com.berss.platform.support.infrastructure.persistence.jpa.repositories.SupportMessageRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/analytics", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "Operational analytics for talent management")
public class AnalyticsController {

    /** Default fallback when the company has no expectedDailyHours configured. */
    private static final double FALLBACK_HOURS_PER_DAY = 8.0;
    /** Default fallback when the company has no workingDaysPerMonth configured. */
    private static final int FALLBACK_WORKING_DAYS_PER_MONTH = 21;
    /** How many months of history the trend endpoint returns. */
    private static final int TREND_MONTHS = 6;

    private static final String[] MONTH_LABELS_ES =
            {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private final EmployeeRepository employeeRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final CompanyQueryRepository companyRepository;

    public AnalyticsController(EmployeeRepository employeeRepository,
                               DailySummaryRepository dailySummaryRepository,
                               SupportMessageRepository supportMessageRepository,
                               CompanyQueryRepository companyRepository) {
        this.employeeRepository = employeeRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/overview/{companyId}")
    public AnalyticsOverviewResource getOverview(@PathVariable Long companyId,
                                                 @RequestParam(required = false) Integer month,
                                                 @RequestParam(required = false) Integer year) {
        var company = new CompanyId(companyId);
        var employees = employeeRepository.findByCompanyId(company);
        var dailies = filterDaily(dailySummaryRepository.findDailySummariesByCompanyId(company), month, year);
        var teams = employees.stream().map(Employee::getTeamName).collect(Collectors.toSet());
        var settings = companyRepository.findById(companyId).orElse(null);

        var aggregates = computeMonthlyAggregates(employees, dailies, settings, month, year);

        var totalInput = aggregates.stream().mapToDouble(MonthlyAggregateResource::totalInput).sum();
        var totalHours = aggregates.stream().mapToInt(MonthlyAggregateResource::totalHours).sum();
        var completedHours = aggregates.stream().mapToDouble(MonthlyAggregateResource::completedHours).sum();
        var completionRate = totalHours == 0 ? 0 : (completedHours * 100.0) / totalHours;
        var averageScore = aggregates.stream()
                .filter(a -> a.dayCount() > 0)
                .mapToDouble(MonthlyAggregateResource::averageScore)
                .average().orElse(0);

        var openSupportMessages = (int) supportMessageRepository.findAll().stream()
                .filter(message -> message.getCompanyId().getValue().equals(companyId))
                .filter(message -> !"RESOLVED".equalsIgnoreCase(message.getStatusAsString()))
                .count();

        return new AnalyticsOverviewResource(
                companyId,
                employees.size(),
                teams.size(),
                round(totalInput),
                round(averageScore),
                totalHours,
                (int) Math.round(completedHours),
                round(completionRate),
                openSupportMessages,
                healthLabel(averageScore, completionRate),
                buildInsights(employees, aggregates, completionRate, averageScore, openSupportMessages)
        );
    }

    @GetMapping("/teams/{companyId}")
    public List<TeamPerformanceResource> getTeams(@PathVariable Long companyId,
                                                  @RequestParam(required = false) Integer month,
                                                  @RequestParam(required = false) Integer year) {
        var company = new CompanyId(companyId);
        var employees = employeeRepository.findByCompanyId(company);
        var dailies = filterDaily(dailySummaryRepository.findDailySummariesByCompanyId(company), month, year);
        var settings = companyRepository.findById(companyId).orElse(null);
        var aggregates = computeMonthlyAggregates(employees, dailies, settings, month, year);

        var aggByEmployee = aggregates.stream()
                .collect(Collectors.groupingBy(MonthlyAggregateResource::employeeId));
        var employeesByTeam = employees.stream().collect(Collectors.groupingBy(Employee::getTeamName));

        return employeesByTeam.entrySet().stream()
                .map(entry -> toTeamPerformance(entry.getKey(), entry.getValue(), aggByEmployee))
                .sorted(Comparator.comparing(TeamPerformanceResource::totalInput).reversed())
                .toList();
    }

    /**
     * Returns one MonthlyAggregateResource per (employee, month) found in the daily records.
     * This is the canonical source for the "Cierre del mes" data — never persisted.
     */
    @GetMapping("/monthly-aggregate/{companyId}")
    public List<MonthlyAggregateResource> getMonthlyAggregate(@PathVariable Long companyId,
                                                              @RequestParam(required = false) Integer month,
                                                              @RequestParam(required = false) Integer year) {
        var company = new CompanyId(companyId);
        var employees = employeeRepository.findByCompanyId(company);
        var dailies = filterDaily(dailySummaryRepository.findDailySummariesByCompanyId(company), month, year);
        var settings = companyRepository.findById(companyId).orElse(null);

        var aggregates = computeMonthlyAggregates(employees, dailies, settings, month, year);
        return aggregates.stream()
                .sorted(Comparator
                        .comparingInt(MonthlyAggregateResource::year).reversed()
                        .thenComparing(Comparator.comparingInt(MonthlyAggregateResource::month).reversed())
                        .thenComparing(MonthlyAggregateResource::employeeName))
                .toList();
    }

    /**
     * 6-month company-wide trend: total input and total hours by month.
     */
    @GetMapping("/trend/{companyId}")
    public List<RevenueTrendPointResource> getTrend(@PathVariable Long companyId,
                                                    @RequestParam(required = false, defaultValue = "6") Integer months) {
        var company = new CompanyId(companyId);
        var dailies = dailySummaryRepository.findDailySummariesByCompanyId(company);
        int span = months == null || months < 1 ? TREND_MONTHS : Math.min(months, 24);

        var today = LocalDate.now();
        var points = new ArrayList<RevenueTrendPointResource>();
        for (int i = span - 1; i >= 0; i--) {
            var ym = YearMonth.from(today).minusMonths(i);
            int y = ym.getYear();
            int m = ym.getMonthValue();
            var monthDailies = dailies.stream()
                    .filter(d -> d.getYear() != null && d.getYear() == y)
                    .filter(d -> d.getMonth() != null && d.getMonth() == m)
                    .toList();
            double totalInput = monthDailies.stream().mapToDouble(DailySummary::getInput).sum();
            double totalHours = monthDailies.stream().mapToDouble(DailySummary::getHoursWorked).sum();
            String label = MONTH_LABELS_ES[m - 1] + " " + (y % 100);
            points.add(new RevenueTrendPointResource(y, m, label, round(totalInput), round(totalHours)));
        }
        return points;
    }

    /**
     * Per-employee revenue / cost / margin breakdown across the requested window.
     * Powers the Dashboard "Top contributors" chart.
     */
    @GetMapping("/contributors/{companyId}")
    public List<EmployeeContributionResource> getContributors(@PathVariable Long companyId,
                                                              @RequestParam(required = false, defaultValue = "6") Integer months) {
        var company = new CompanyId(companyId);
        var employees = employeeRepository.findByCompanyId(company);
        var settings = companyRepository.findById(companyId).orElse(null);
        var dailies = dailySummaryRepository.findDailySummariesByCompanyId(company);

        int span = months == null || months < 1 ? TREND_MONTHS : Math.min(months, 24);
        var today = LocalDate.now();
        var earliest = YearMonth.from(today).minusMonths(span - 1L);

        var employeeById = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
        double defaultCostRate = settings == null ? 0.0 : settings.getDefaultHourlyCostOrZero();

        Map<Long, double[]> byEmployee = new HashMap<>(); // [hours, input, cost]
        for (var d : dailies) {
            if (d.getYear() == null || d.getMonth() == null) continue;
            var ym = YearMonth.of(d.getYear(), d.getMonth());
            if (ym.isBefore(earliest)) continue;
            var emp = employeeById.get(d.getEmployeeId());
            if (emp == null) continue;

            double[] acc = byEmployee.computeIfAbsent(emp.getId(), k -> new double[3]);
            double hours = d.getHoursWorked();
            double input = d.getInput();
            double costRate = emp.getHourlyCost() != null && emp.getHourlyCost() > 0 ? emp.getHourlyCost() : defaultCostRate;
            acc[0] += hours;
            acc[1] += input;
            acc[2] += hours * costRate;
        }

        return byEmployee.entrySet().stream()
                .map(entry -> {
                    var emp = employeeById.get(entry.getKey());
                    double hours = entry.getValue()[0];
                    double input = entry.getValue()[1];
                    double cost = entry.getValue()[2];
                    double margin = input - cost;
                    double marginRate = input > 0 ? (margin / input) * 100.0 : 0.0;
                    return new EmployeeContributionResource(
                            emp.getId(),
                            emp.getFirstName() + " " + emp.getLastName(),
                            emp.getTeamName(),
                            round(hours),
                            round(input),
                            round(cost),
                            round(margin),
                            round(marginRate)
                    );
                })
                .sorted(Comparator.comparingDouble(EmployeeContributionResource::totalInput).reversed())
                .toList();
    }

    // ---------------------------------------------------------------- helpers

    private TeamPerformanceResource toTeamPerformance(String teamName,
                                                      List<Employee> teamEmployees,
                                                      Map<Long, List<MonthlyAggregateResource>> aggByEmployee) {
        var teamAggregates = teamEmployees.stream()
                .flatMap(e -> aggByEmployee.getOrDefault(e.getId(), List.of()).stream())
                .toList();
        var totalInput = teamAggregates.stream().mapToDouble(MonthlyAggregateResource::totalInput).sum();
        var totalHours = teamAggregates.stream().mapToInt(MonthlyAggregateResource::totalHours).sum();
        var completedHours = teamAggregates.stream().mapToDouble(MonthlyAggregateResource::completedHours).sum();
        var completionRate = totalHours == 0 ? 0 : (completedHours * 100.0) / totalHours;
        var averageScore = teamAggregates.stream()
                .filter(a -> a.dayCount() > 0)
                .mapToDouble(MonthlyAggregateResource::averageScore)
                .average().orElse(0);

        return new TeamPerformanceResource(
                teamName,
                teamEmployees.size(),
                round(totalInput),
                round(averageScore),
                (int) Math.round(completedHours),
                totalHours,
                round(completionRate)
        );
    }

    private List<DailySummary> filterDaily(List<DailySummary> dailies, Integer month, Integer year) {
        return dailies.stream()
                .filter(d -> month == null || (d.getMonth() != null && d.getMonth().equals(month)))
                .filter(d -> year == null || (d.getYear() != null && d.getYear().equals(year)))
                .toList();
    }

    /**
     * Aggregates daily summaries by (employeeId, year, month). The expected monthly hours
     * come from the company settings (or sensible defaults), so the cumplimiento target
     * is no longer hard-coded.
     */
    private List<MonthlyAggregateResource> computeMonthlyAggregates(List<Employee> employees,
                                                                    List<DailySummary> dailies,
                                                                    Company settings,
                                                                    Integer monthFilter,
                                                                    Integer yearFilter) {
        var employeeById = employees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));
        Set<Long> employeeIds = employeeById.keySet();

        record Key(long employeeId, int year, int month) {}

        Map<Key, List<DailySummary>> grouped = new HashMap<>();
        for (var d : dailies) {
            if (!employeeIds.contains(d.getEmployeeId())) continue;
            if (d.getYear() == null || d.getMonth() == null) continue;
            var key = new Key(d.getEmployeeId(), d.getYear(), d.getMonth());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }

        double hoursPerDay = settings == null ? FALLBACK_HOURS_PER_DAY : settings.getExpectedDailyHoursOrDefault();
        int workingDaysPerMonth = settings == null ? FALLBACK_WORKING_DAYS_PER_MONTH : settings.getWorkingDaysPerMonthOrDefault();
        int totalHoursTarget = (int) Math.round(hoursPerDay * workingDaysPerMonth);
        double defaultCostRate = settings == null ? 0.0 : settings.getDefaultHourlyCostOrZero();

        return grouped.entrySet().stream()
                .filter(e -> monthFilter == null || e.getKey().month() == monthFilter)
                .filter(e -> yearFilter == null || e.getKey().year() == yearFilter)
                .map(entry -> {
                    var k = entry.getKey();
                    var rows = entry.getValue();
                    double completedHours = rows.stream().mapToDouble(DailySummary::getHoursWorked).sum();
                    double totalInput = rows.stream().mapToDouble(DailySummary::getInput).sum();
                    double averageScore = rows.stream().mapToInt(DailySummary::getScore).average().orElse(0);
                    double completionRate = totalHoursTarget == 0 ? 0 : (completedHours * 100.0) / totalHoursTarget;

                    var employee = employeeById.get(k.employeeId());
                    double costRate = employee != null && employee.getHourlyCost() != null && employee.getHourlyCost() > 0
                            ? employee.getHourlyCost()
                            : defaultCostRate;
                    double totalCost = completedHours * costRate;
                    double margin = totalInput - totalCost;

                    var teamName = employee != null ? employee.getTeamName() : "";
                    var fullName = employee != null
                            ? employee.getFirstName() + " " + employee.getLastName()
                            : ("Empleado #" + k.employeeId());

                    return new MonthlyAggregateResource(
                            k.employeeId(),
                            fullName,
                            teamName,
                            k.year(),
                            k.month(),
                            rows.size(),
                            round(completedHours),
                            totalHoursTarget,
                            round(completionRate),
                            round(totalInput),
                            round(totalCost),
                            round(margin),
                            round(averageScore)
                    );
                })
                .toList();
    }

    /**
     * Returns stable, language-agnostic insight codes. The frontend resolves each code
     * to localized copy, so the dashboard reads naturally in both Spanish and English.
     */
    private List<String> buildInsights(List<Employee> employees,
                                       List<MonthlyAggregateResource> aggregates,
                                       double completionRate,
                                       double averageScore,
                                       int openSupportMessages) {
        var insights = new ArrayList<String>();

        if (employees.isEmpty()) {
            insights.add("REGISTER_EMPLOYEES");
            return insights;
        }
        if (aggregates.isEmpty()) {
            insights.add("NO_DAILY_RECORDS");
        }
        if (completionRate >= 90) {
            insights.add("STRONG_COMPLETION");
        } else if (completionRate > 0) {
            insights.add("LOW_COMPLETION");
        }
        if (averageScore < 7 && averageScore > 0) {
            insights.add("LOW_SCORE");
        }
        if (openSupportMessages > 0) {
            insights.add("OPEN_SUPPORT");
        }
        if (insights.isEmpty()) {
            insights.add("STABLE");
        }

        return insights;
    }

    /** Stable health code resolved to localized copy on the frontend. */
    private String healthLabel(double averageScore, double completionRate) {
        if (averageScore >= 8 && completionRate >= 85) return "HEALTHY";
        if (averageScore >= 6 && completionRate >= 65) return "WATCH";
        return "ATTENTION";
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
