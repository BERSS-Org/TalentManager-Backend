package com.berss.platform.reports.interfaces.rest.transform;

import com.berss.platform.reports.domain.model.aggregates.Report;
import com.berss.platform.reports.interfaces.rest.resources.ReportResource;

public class ReportResourceFromEntityAssembler {
    public static ReportResource toResourcefromEntity(Report entity) {
        return new ReportResource(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCompanyId(),
                entity.getEmployeeId(),
                entity.getYear(),
                entity.getMonth()
        );
    }
}
