package com.berss.platform.employees.interfaces.rest.transform;

import com.berss.platform.employees.domain.model.aggregates.Employee;
import com.berss.platform.employees.interfaces.rest.resources.EmployeeResource;

public class EmployeeResourceFromEntityAssembler {
    public static EmployeeResource toResourceFromEntity(Employee entity) {
        return new EmployeeResource(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getOccupation(),
                entity.getEntryDate(),
                entity.getTeamName(),
                entity.getCompanyId(),
                entity.getHourlyRate(),
                entity.getHourlyCost()
        );
    }
}
