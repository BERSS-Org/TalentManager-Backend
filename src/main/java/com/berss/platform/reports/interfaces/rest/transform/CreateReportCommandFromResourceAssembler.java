package com.berss.platform.reports.interfaces.rest.transform;

import com.berss.platform.reports.domain.model.commands.CreateReportCommand;
import com.berss.platform.reports.interfaces.rest.resources.CreateReportResource;

public class CreateReportCommandFromResourceAssembler {
    public static CreateReportCommand toCommandFromResource(CreateReportResource resource) {
        return new CreateReportCommand(
                resource.title(),
                resource.content(),
                resource.companyId(),
                resource.employeeId(),
                resource.year(),
                resource.month()
        );
    }
}
