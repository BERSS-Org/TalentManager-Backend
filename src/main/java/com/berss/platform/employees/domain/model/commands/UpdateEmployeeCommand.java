package com.berss.platform.employees.domain.model.commands;

import java.time.LocalDate;

public record UpdateEmployeeCommand(
        Long employeeId,
        String firstName,
        String lastName,
        String occupation,
        LocalDate entryDate,
        String teamName,
        Double hourlyRate,
        Double hourlyCost
) {
    public UpdateEmployeeCommand {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("employeeId cannot be null or less than 1");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        if (occupation == null || occupation.isBlank()) {
            throw new IllegalArgumentException("occupation cannot be null or blank");
        }
        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate cannot be null");
        }
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("teamName cannot be null or blank");
        }
        if (hourlyRate != null && hourlyRate < 0) {
            throw new IllegalArgumentException("hourlyRate cannot be negative");
        }
        if (hourlyCost != null && hourlyCost < 0) {
            throw new IllegalArgumentException("hourlyCost cannot be negative");
        }
    }
}
