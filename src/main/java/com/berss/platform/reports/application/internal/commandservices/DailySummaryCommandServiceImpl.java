package com.berss.platform.reports.application.internal.commandservices;

import com.berss.platform.business.infrastructure.persistence.jpa.repositories.CompanyQueryRepository;
import com.berss.platform.employees.infrastructure.persistence.jpa.repositories.EmployeeRepository;
import com.berss.platform.reports.domain.model.aggregates.DailySummary;
import com.berss.platform.reports.domain.model.commands.CreateDailySummaryCommand;
import com.berss.platform.reports.domain.model.commands.DeleteDailySummaryCommand;
import com.berss.platform.reports.domain.model.commands.UpdateDailySummaryCommand;
import com.berss.platform.reports.domain.services.DailySummaryCommandService;
import com.berss.platform.reports.infrastructure.persistence.jpa.repositories.DailySummaryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DailySummaryCommandServiceImpl implements DailySummaryCommandService {

    private final DailySummaryRepository repository;
    private final EmployeeRepository employeeRepository;
    private final CompanyQueryRepository companyRepository;

    public DailySummaryCommandServiceImpl(DailySummaryRepository repository,
                                          EmployeeRepository employeeRepository,
                                          CompanyQueryRepository companyRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public Long handle(CreateDailySummaryCommand command) {
        repository.findByEmployeeCompanyAndDate(
                command.employeeId(),
                command.companyId(),
                command.day(),
                command.month(),
                command.year()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "A daily summary already exists for this employee and date. Update the existing record instead."
            );
        });

        double inputAmount = resolveInputAmount(command);

        DailySummary summary = new DailySummary(
                command.employeeId(),
                command.companyId(),
                command.day(),
                command.month(),
                command.year(),
                command.entryTime(),
                command.exitTime(),
                inputAmount,
                command.score()
        );
        return repository.save(summary).getId();
    }

    @Override
    public Optional<DailySummary> handle(UpdateDailySummaryCommand command) {
        var optional = repository.findById(command.dailySummaryId());
        if (optional.isEmpty()) return Optional.empty();

        var summary = optional.get();
        summary.updateEntryTime(command.entryTime());
        summary.updateExitTime(command.exitTime());
        summary.updateScore(command.score());

        double explicitInput = command.inputAmount() == null ? 0.0 : command.inputAmount();
        if (explicitInput > 0) {
            summary.updateInput(explicitInput);
        } else {
            summary.updateInput(deriveInputAmount(summary.getEmployeeId(), summary.getCompanyId(), summary.getHoursWorked()));
        }

        return Optional.of(repository.save(summary));
    }

    @Override
    public void handle(DeleteDailySummaryCommand command) {
        repository.deleteById(command.dailySummaryId());
    }

    private double resolveInputAmount(CreateDailySummaryCommand command) {
        double provided = command.inputAmount() == null ? 0.0 : command.inputAmount();
        if (provided > 0) return provided;
        double hoursWorked = Math.max(0.0, command.exitTime() - command.entryTime());
        return deriveInputAmount(command.employeeId(), command.companyId(), hoursWorked);
    }

    /**
     * Income = hoursWorked * (employee.hourlyRate fall back to company.defaultHourlyRate).
     * Returns 0 when neither is configured — the manager just records hours.
     */
    private double deriveInputAmount(long employeeId, long companyId, double hoursWorked) {
        if (hoursWorked <= 0) return 0.0;
        double rate = employeeRepository.findById(employeeId)
                .map(e -> e.getHourlyRate() != null ? e.getHourlyRate() : 0.0)
                .orElse(0.0);
        if (rate <= 0) {
            rate = companyRepository.findById(companyId)
                    .map(c -> c.getDefaultHourlyRateOrZero())
                    .orElse(0.0);
        }
        if (rate <= 0) return 0.0;
        return Math.round(rate * hoursWorked * 100.0) / 100.0;
    }
}
