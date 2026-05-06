package com.berss.platform.employees.interfaces.rest.resources;


import java.time.LocalDate;

public record CreateEmployeeResource(
        String firstName,
        String lastName,
        String occupation,
        LocalDate registrationDate,
        String teamName,
        Long companyId,
        Double hourlyRate,
        Double hourlyCost
) {
    public CreateEmployeeResource {

    }
}
