package com.berss.platform.reports.interfaces.rest.transform;

import com.berss.platform.reports.domain.model.aggregates.DailySummary;
import com.berss.platform.reports.interfaces.rest.resources.DailySummaryResource;

public class DailySummaryResourceFromEntityAssembler {
    /**
     * Converts a DailySummary entity to a DailySummaryResource.
     *
     * @param entity The {@link DailySummary} entity to convert.
     * @return The {@link DailySummaryResource} resource that results from the conversion.
     */

    public static DailySummaryResource toResourcefromEntity(DailySummary entity) {
        return new DailySummaryResource(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getCompanyId(),
                entity.getDay(),
                entity.getMonth(),
                entity.getYear(),
                entity.getEntryTime(),
                entity.getExitTime(),
                entity.getHoursWorked(),
                entity.getInput(),
                entity.getScore()
        );
    }
}
