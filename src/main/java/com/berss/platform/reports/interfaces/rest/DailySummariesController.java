package com.berss.platform.reports.interfaces.rest;

import com.berss.platform.reports.domain.model.commands.DeleteDailySummaryCommand;
import com.berss.platform.reports.domain.model.queries.GetAllDailySummariesQuery;
import com.berss.platform.reports.domain.model.queries.GetDailySummariesByCompanyIdQuery;
import com.berss.platform.reports.domain.model.queries.GetDailySummariesByEmployeeIdQuery;
import com.berss.platform.reports.domain.model.queries.GetDailySummaryByIdQuery;
import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.reports.domain.services.DailySummaryCommandService;
import com.berss.platform.reports.domain.services.DailySummaryQueryService;
import com.berss.platform.reports.interfaces.rest.resources.CreateDailySummaryResource;
import com.berss.platform.reports.interfaces.rest.resources.DailySummaryResource;
import com.berss.platform.reports.interfaces.rest.resources.UpdateDailySummaryResource;
import com.berss.platform.reports.interfaces.rest.transform.CreateDailySummaryCommandFromResourceAssembler;
import com.berss.platform.reports.interfaces.rest.transform.DailySummaryResourceFromEntityAssembler;
import com.berss.platform.reports.interfaces.rest.transform.UpdateDailySummaryCommandFromResourceAssembler;

import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * DailySummariesController
 * <p>
 *     All Daily summary related endpoints.
 * </p>
 */

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/daily-summaries", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Daily Summaries", description = "Available Daily Summary Endpoints")
public class DailySummariesController {

    private final DailySummaryCommandService commandService;
    private final DailySummaryQueryService queryService;

    public DailySummariesController(DailySummaryCommandService commandService, DailySummaryQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a new daily summary", description = "Create a new daily summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Daily summary created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<DailySummaryResource> createDailySummary(@RequestBody CreateDailySummaryResource resource) {
        var command = CreateDailySummaryCommandFromResourceAssembler.toCommandFromResource(resource);
        var DailySummaryId = commandService.handle(command);
        if (DailySummaryId == null || DailySummaryId == 0L) return ResponseEntity.badRequest().build();

        var query = new GetDailySummaryByIdQuery(DailySummaryId);
        var result = queryService.handle(query);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = DailySummaryResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return new ResponseEntity<>(summaryResource, HttpStatus.CREATED);
    }

    @GetMapping("/{dailySummaryId}")
    @Operation(summary = "Get daily summary by ID", description = "Get a specific daily summary by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summary found"),
            @ApiResponse(responseCode = "404", description = "Daily summary not found")
    })
    public ResponseEntity<DailySummaryResource> getDailySummaryById(@PathVariable Long dailySummaryId) {
        var query = new GetDailySummaryByIdQuery(dailySummaryId);
        var result = queryService.handle(query);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = DailySummaryResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return ResponseEntity.ok(summaryResource);
    }

    @GetMapping
    @Operation(summary = "Get all daily summaries", description = "Retrieve all existing daily summaries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summaries found"),
            @ApiResponse(responseCode = "404", description = "No Daily summaries found")
    })
    public ResponseEntity<List<DailySummaryResource>> getAllDailySummaries() {
        var summaries = queryService.handle(new GetAllDailySummariesQuery());

        var resources = summaries.stream()
                .map(DailySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{dailySummaryId}")
    @Operation(summary = "Update a daily summary", description = "Update an existing daily summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summary updated"),
            @ApiResponse(responseCode = "404", description = "Daily summary not found")
    })
    public ResponseEntity<DailySummaryResource> updateDailySummary(@PathVariable Long dailySummaryId, @RequestBody UpdateDailySummaryResource resource) {
        var command = UpdateDailySummaryCommandFromResourceAssembler.toCommandfromResource(dailySummaryId, resource);
        var updated = commandService.handle(command);
        if (updated.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = DailySummaryResourceFromEntityAssembler.toResourcefromEntity(updated.get());
        return ResponseEntity.ok(summaryResource);
    }

    @DeleteMapping("/{dailySummaryId}")
    @Operation(summary = "Delete a daily summary", description = "Delete an existing daily summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summary deleted"),
            @ApiResponse(responseCode = "404", description = "Daily summary not found")
    })
    public ResponseEntity<?> deleteDailySummary(@PathVariable Long dailySummaryId) {
        commandService.handle(new DeleteDailySummaryCommand(dailySummaryId));
        return ResponseEntity.ok("Daily summary successfully deleted");
    }

    @GetMapping("/by-company/{companyId}")
    @Operation(summary = "Get daily summaries by company ID", description = "Retrieve daily summaries for a specific company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summaries found"),
            @ApiResponse(responseCode = "404", description = "No Daily summaries found for company")
    })
    public ResponseEntity<List<DailySummaryResource>> getDailySummariesByCompanyIdQuery(@PathVariable Long companyId) {
        var companyId_ = new CompanyId(companyId);
        var query = new GetDailySummariesByCompanyIdQuery(companyId_);
        var summaries = queryService.handle(query);

        var resources = summaries.stream()
                .map(DailySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/by-employee/{employeeId}")
    @Operation(summary = "Get daily summaries by employee ID", description = "Retrieve daily summaries for a specific employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily summaries found"),
            @ApiResponse(responseCode = "404", description = "No Daily summaries found for employee")
    })
    public ResponseEntity<List<DailySummaryResource>> getDailySummariesByEmployeeId(@PathVariable Long employeeId) {
        var employeeId_ = new EmployeeId(employeeId);
        var query = new GetDailySummariesByEmployeeIdQuery(employeeId_);
        var summaries = queryService.handle(query);

        var resources = summaries.stream()
                .map(DailySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
