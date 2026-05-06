package com.berss.platform.employees.domain.model.commands;

import java.time.LocalDate;

/**
 * Command for creating an Employee.
 */
public record CreateEmployeeCommand(
        String firstName,
        String lastName,
        String occupation,
        LocalDate entryDate,
        String teamName,
        Long companyId,
        Double hourlyRate,
        Double hourlyCost
) {

    public CreateEmployeeCommand {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("firstName cannot be null or empty");
        }
        if (firstName.length() < 2 || firstName.length() > 50) {
            throw new IllegalArgumentException("firstName must be between 2 and 50 characters");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("lastName cannot be null or empty");
        }
        if (lastName.length() < 2 || lastName.length() > 50) {
            throw new IllegalArgumentException("lastName must be between 2 and 50 characters");
        }

        if (occupation == null || occupation.trim().isEmpty()) {
            throw new IllegalArgumentException("occupation cannot be null or empty");
        }

        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate cannot be null");
        }

        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("teamName cannot be null or empty");
        }
        if (teamName.length() < 2 || teamName.length() > 50) {
            throw new IllegalArgumentException("teamName must be between 2 and 50 characters");
        }

        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("companyId must be greater than 0");
        }

        if (hourlyRate != null && hourlyRate < 0) {
            throw new IllegalArgumentException("hourlyRate cannot be negative");
        }
        if (hourlyCost != null && hourlyCost < 0) {
            throw new IllegalArgumentException("hourlyCost cannot be negative");
        }
    }
}
