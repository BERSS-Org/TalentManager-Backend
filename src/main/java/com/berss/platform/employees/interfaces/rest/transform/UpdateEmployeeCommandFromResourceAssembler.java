package com.berss.platform.employees.interfaces.rest.transform;

import com.berss.platform.employees.domain.model.commands.UpdateEmployeeCommand;
import com.berss.platform.employees.interfaces.rest.resources.UpdateEmployeeResource;

public class UpdateEmployeeCommandFromResourceAssembler {
    public static UpdateEmployeeCommand toCommandFromResource(Long employeeId, UpdateEmployeeResource resource) {
        return new UpdateEmployeeCommand(
                employeeId,
                resource.firstName(),
                resource.lastName(),
                resource.occupation(),
                resource.registrationDate(),
                resource.teamName(),
                resource.hourlyRate(),
                resource.hourlyCost()
        );
    }
}
