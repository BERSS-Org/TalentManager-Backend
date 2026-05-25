package com.berss.platform.config;

import com.berss.platform.business.domain.model.aggregates.Manager;
import com.berss.platform.business.domain.model.entities.Company;
import com.berss.platform.business.domain.model.valueobjects.CompanyStatus;
import com.berss.platform.business.infrastructure.persistence.jpa.repositories.ManagerRepository;
import com.berss.platform.employees.domain.model.aggregates.Employee;
import com.berss.platform.employees.infrastructure.persistence.jpa.repositories.EmployeeRepository;
import com.berss.platform.iam.domain.model.aggregates.User;
import com.berss.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.berss.platform.reports.domain.model.aggregates.DailySummary;
import com.berss.platform.reports.domain.model.aggregates.Report;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.DailySummaryRepository;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.ReportRepository;
import com.berss.platform.support.domain.model.aggregates.SupportMessage;
import com.berss.platform.support.domain.model.entities.Status;
import com.berss.platform.support.domain.model.valueobjects.SupportStatus;
import com.berss.platform.support.infrastructure.persistence.jpa.repositories.StatusRepository;
import com.berss.platform.support.infrastructure.persistence.jpa.repositories.SupportMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class LocalDataSeeder implements CommandLineRunner {

    private static final String DEMO_USERNAME = "demo@talentmanager.local";

    @Value("${talentmanager.seed.enabled:true}")
    private boolean seedEnabled;

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final ReportRepository reportRepository;
    private final StatusRepository statusRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalDataSeeder(UserRepository userRepository,
                           ManagerRepository managerRepository,
                           EmployeeRepository employeeRepository,
                           DailySummaryRepository dailySummaryRepository,
                           ReportRepository reportRepository,
                           StatusRepository statusRepository,
                           SupportMessageRepository supportMessageRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.managerRepository = managerRepository;
        this.employeeRepository = employeeRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.reportRepository = reportRepository;
        this.statusRepository = statusRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var pendingStatus = ensureStatus(SupportStatus.PENDING);
        ensureStatus(SupportStatus.IN_PROGRESS);
        ensureStatus(SupportStatus.RESOLVED);

        if (!seedEnabled || userRepository.existsByUsername(DEMO_USERNAME)) {
            return;
        }

        var company = new Company(
                "Aster Talent Studio",
                "Consultora de equipos comerciales y operaciones internas.",
                "ops@astertalent.local",
                CompanyStatus.ACTIVE
        );
        var manager = managerRepository.save(new Manager("Lucia", "Mendoza", company));
        var companyId = manager.getCompany().getId();

        userRepository.save(new User(
                DEMO_USERNAME,
                passwordEncoder.encode("Talent1234"),
                manager.getId(),
                companyId
        ));

        record EmployeeProfile(String firstName, String lastName, String role, String team,
                               int monthsTenure, double baseInput, double baseScore, double cap,
                               double hourlyRate, double hourlyCost) {}

        var profiles = List.of(
                new EmployeeProfile("Sofia", "Ramirez", "People Operations Lead", "Operations", 36, 320, 9.0, 0.96, 65.0, 38.0),
                new EmployeeProfile("Mateo", "Salazar", "Sales Strategist",       "Growth",     24, 410, 8.2, 0.88, 80.0, 42.0),
                new EmployeeProfile("Valeria", "Torres", "Talent Analyst",        "Operations", 18, 270, 8.4, 0.92, 55.0, 30.0),
                new EmployeeProfile("Diego", "Paredes", "Account Executive",      "Growth",     11, 360, 7.0, 0.78, 70.0, 36.0),
                new EmployeeProfile("Camila", "Vega",   "Customer Success",       "Experience", 8,  240, 9.1, 0.97, 50.0, 28.0),
                new EmployeeProfile("Nicolas", "Rojas", "Implementation Specialist", "Experience", 6, 215, 7.6, 0.84, 45.0, 26.0)
        );

        var employees = new ArrayList<Employee>();
        for (var p : profiles) {
            employees.add(employeeRepository.save(new Employee(
                    p.firstName(), p.lastName(), p.role(),
                    LocalDate.now().minusMonths(p.monthsTenure()),
                    p.team(), companyId,
                    p.hourlyRate(), p.hourlyCost()
            )));
        }

        // Default operating settings for the demo company.
        company.setExpectedDailyHours(8.0);
        company.setWorkingDaysPerMonth(21);
        company.setCurrencyCode("USD");
        company.setDefaultHourlyRate(50.0);
        company.setDefaultHourlyCost(28.0);

        // Realistic 6 months of daily records (workdays only, mild seasonality).
        var random = new Random(42);
        var today = LocalDate.now();
        var dailies = new ArrayList<DailySummary>();
        for (int monthsBack = 5; monthsBack >= 0; monthsBack--) {
            var ym = YearMonth.from(today).minusMonths(monthsBack);
            var seasonality = 1.0 + 0.04 * (5 - monthsBack); // recent months trend up
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                var date = ym.atDay(day);
                if (date.isAfter(today)) break;
                var dow = date.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) continue;

                for (int i = 0; i < employees.size(); i++) {
                    var employee = employees.get(i);
                    var p = profiles.get(i);
                    if (random.nextDouble() > p.cap()) continue; // simulate occasional absences

                    double entryTime = 8.5 + random.nextInt(2) * 0.5;          // 8:30 / 9:00 / 9:30
                    double dailyHours = 7.0 + random.nextDouble() * 2.0;        // 7..9 worked
                    double exitTime = Math.min(20.0, entryTime + dailyHours);
                    double inputAmount = Math.round(p.baseInput() * seasonality * (0.85 + random.nextDouble() * 0.4));
                    int score = (int) Math.round(Math.max(4, Math.min(10, p.baseScore() + (random.nextGaussian() * 0.8))));

                    dailies.add(new DailySummary(
                            employee.getId(), companyId,
                            day, ym.getMonthValue(), ym.getYear(),
                            entryTime, exitTime,
                            inputAmount, score
                    ));
                }
            }
        }
        dailySummaryRepository.saveAll(dailies);

        // Seed report uses the most recent month for the top performer.
        var sofia = employees.get(0);
        var lastMonth = YearMonth.from(today);
        reportRepository.save(new Report(
                "Pulso operativo del mes",
                "Growth sostiene el mayor ingreso, Operations mantiene consistencia y Experience destaca en cumplimiento. " +
                        "El foco recomendado es reforzar el acompañamiento comercial para subir horas completadas sin perder calidad.",
                companyId,
                sofia.getId(),
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        ));

        supportMessageRepository.save(new SupportMessage(
                "Necesitamos revisar permisos de usuarios para que el equipo de operaciones pueda cargar resúmenes.",
                companyId,
                Instant.now().minus(Duration.ofDays(1)),
                Instant.now().minus(Duration.ofDays(1)).plus(Duration.ofMinutes(15)),
                pendingStatus
        ));
    }

    private Status ensureStatus(SupportStatus status) {
        return statusRepository.findByName(status)
                .orElseGet(() -> statusRepository.save(new Status(status)));
    }
}
