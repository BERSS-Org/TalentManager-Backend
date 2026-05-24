package com.berss.platform.employees.application.internal.commandservices;

import com.berss.platform.employees.domain.model.aggregates.Employee;
import com.berss.platform.employees.domain.model.commands.CreateEmployeeCommand;
import com.berss.platform.employees.domain.model.commands.DeleteEmployeeCommand;
import com.berss.platform.employees.domain.model.commands.UpdateEmployeeCommand;
import com.berss.platform.employees.domain.model.events.EmployeeDeletedEvent;
import com.berss.platform.employees.domain.services.EmployeeCommandService;
import com.berss.platform.employees.infrastructure.persistence.jpa.repositories.EmployeeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EmployeeCommandServiceImpl implements EmployeeCommandService {
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeCommandServiceImpl(EmployeeRepository employeeRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long handle(CreateEmployeeCommand command) {
        var employee = new Employee(command);
        try {
            employeeRepository.save(employee);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving employee: %s".formatted(e.getMessage()));
        }
        return employee.getId();
    }

    @Override
    public Optional<Employee> handle(UpdateEmployeeCommand command) {
        var result = employeeRepository.findById(command.employeeId());
        if (result.isEmpty())
            throw new IllegalArgumentException("Employee with id %s not found".formatted(command.employeeId()));
        var employeeToUpdate = result.get();
        try {
            employeeToUpdate.updateInformation(command.firstName(), command.lastName(), command.occupation(), command.entryDate(), command.teamName());
            // Only touch compensation when the request actually carries it. The Employees
            // page edits profile data without rates, so omitting this guard would wipe the
            // hourlyRate/hourlyCost configured in Settings (revenue/cost/margin would reset to 0).
            if (command.hourlyRate() != null || command.hourlyCost() != null) {
                employeeToUpdate.updateCompensation(
                        command.hourlyRate() != null ? command.hourlyRate() : employeeToUpdate.getHourlyRate(),
                        command.hourlyCost() != null ? command.hourlyCost() : employeeToUpdate.getHourlyCost()
                );
            }
            var updatedEmployee = employeeRepository.save(employeeToUpdate);
            return Optional.of(updatedEmployee);
        }catch (Exception e){
            throw new IllegalArgumentException("Error while updating employee: %s".formatted(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public void handle(DeleteEmployeeCommand command) {
        if (!employeeRepository.existsById(command.employeeId())) {
            throw new IllegalArgumentException("Employee with id %s not found".formatted(command.employeeId()));
        }
        try {
            employeeRepository.deleteById(command.employeeId());
            // Let other contexts (reports) clean up data anchored to this employee
            // so no orphaned daily summaries or reports remain.
            eventPublisher.publishEvent(new EmployeeDeletedEvent(command.employeeId()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting employee: %s".formatted(e.getMessage()));
        }
    }

}
