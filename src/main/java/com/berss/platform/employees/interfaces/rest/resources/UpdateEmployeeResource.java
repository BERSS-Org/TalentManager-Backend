package com.berss.platform.employees.interfaces.rest.resources;


import java.time.LocalDate;

public record UpdateEmployeeResource(
        String firstName,
        String lastName,
        String occupation,
        LocalDate registrationDate,
        String teamName,
        Double hourlyRate,
        Double hourlyCost
) {
    public UpdateEmployeeResource {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("FirstName is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("LastName is required");
        }
        if (occupation == null || occupation.isBlank()) {
            throw new IllegalArgumentException("Occupation is required");
        }
        if (registrationDate == null) {
            throw new IllegalArgumentException("RegistrationDate is required");
        }
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("TeamName is required");
        }
        if (hourlyRate != null && hourlyRate < 0) {
            throw new IllegalArgumentException("hourlyRate cannot be negative");
        }
        if (hourlyCost != null && hourlyCost < 0) {
            throw new IllegalArgumentException("hourlyCost cannot be negative");
        }
    }
}
