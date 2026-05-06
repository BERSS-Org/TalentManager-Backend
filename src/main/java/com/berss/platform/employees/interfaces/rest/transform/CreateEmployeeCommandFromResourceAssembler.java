package com.berss.platform.employees.interfaces.rest.transform;

import com.berss.platform.employees.domain.model.commands.CreateEmployeeCommand;
import com.berss.platform.employees.interfaces.rest.resources.CreateEmployeeResource;

public class CreateEmployeeCommandFromResourceAssembler {
    public static CreateEmployeeCommand toCommandFromResource(CreateEmployeeResource resource) {
        return new CreateEmployeeCommand(
                resource.firstName(),
                resource.lastName(),
                resource.occupation(),
                resource.registrationDate(),
                resource.teamName(),
                resource.companyId(),
                resource.hourlyRate(),
                resource.hourlyCost()
        );
    }
}
