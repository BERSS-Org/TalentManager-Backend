package com.berss.platform.employees.application.internal.commandservices;

import com.berss.platform.employees.domain.model.aggregates.Employee;
import com.berss.platform.employees.domain.model.commands.CreateEmployeeCommand;
import com.berss.platform.employees.domain.model.commands.DeleteEmployeeCommand;
import com.berss.platform.employees.domain.model.commands.UpdateEmployeeCommand;
import com.berss.platform.employees.domain.services.EmployeeCommandService;
import com.berss.platform.employees.infrastructure.persistence.jpa.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeCommandServiceImpl implements EmployeeCommandService {
    private final EmployeeRepository employeeRepository;

    public EmployeeCommandServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Long handle(CreateEmployeeCommand command) {
        var employee = new Employee(command);
        try {
            employeeRepository.save(employee);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving course: %s".formatted(e.getMessage()));
        }
        return employee.getId();
    }

    @Override
    public Optional<Employee> handle(UpdateEmployeeCommand command) {
        var result = employeeRepository.findById(command.employeeId());
        if (result.isEmpty())
            throw new IllegalArgumentException("Employeee with id %s not found".formatted(command.employeeId()));
        var employeeToUpdate = result.get();
        try {
            employeeToUpdate.updateInformation(command.firstName(), command.lastName(), command.occupation(), command.entryDate(), command.teamName());
            employeeToUpdate.updateCompensation(command.hourlyRate(), command.hourlyCost());
            var updatedEmployee = employeeRepository.save(employeeToUpdate);
            return Optional.of(updatedEmployee);
        }catch (Exception e){
            throw new IllegalArgumentException("Error while updating employee: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public void handle(DeleteEmployeeCommand command) {
        if (!employeeRepository.existsById(command.employeeId())) {
            throw new IllegalArgumentException("Employee with id %s not found".formatted(command.employeeId()));
        }
        try {
            employeeRepository.deleteById(command.employeeId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting employee: %s".formatted(e.getMessage()));
        }
    }

}
