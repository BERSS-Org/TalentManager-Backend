package com.berss.platform.reports.interfaces.rest;

import com.berss.platform.reports.domain.model.commands.DeleteMonthlySummaryCommand;
import com.berss.platform.reports.domain.model.queries.GetAllMonthlySummariesQuery;
import com.berss.platform.reports.domain.model.queries.GetMonthlySummariesByCompanyIdQuery;
import com.berss.platform.reports.domain.model.queries.GetMonthlySummariesByEmployeeIdQuery;
import com.berss.platform.reports.domain.model.queries.GetMonthlySummaryByIdQuery;
import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.reports.domain.services.MonthlySummaryCommandService;
import com.berss.platform.reports.domain.services.MonthlySummaryQueryService;
import com.berss.platform.reports.interfaces.rest.resources.CreateMonthlySummaryResource;
import com.berss.platform.reports.interfaces.rest.resources.MonthlySummaryResource;
import com.berss.platform.reports.interfaces.rest.resources.UpdateMonthlySummaryResource;
import com.berss.platform.reports.interfaces.rest.transform.CreateMonthlySummaryCommandFromResourceAssembler;
import com.berss.platform.reports.interfaces.rest.transform.MonthlySummaryResourceFromEntityAssembler;
import com.berss.platform.reports.interfaces.rest.transform.UpdateMonthlySummaryCommandFromResourceAssembler;

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
 * MonthlySummariesController
 * <p>
 *     All monthly summary related endpoints.
 * </p>
 */

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/monthly-summaries", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Monthly Summaries", description = "Available Monthly Summary Endpoints")
public class MonthlySummariesController {

    private final MonthlySummaryCommandService commandService;
    private final MonthlySummaryQueryService queryService;

    public MonthlySummariesController(MonthlySummaryCommandService commandService, MonthlySummaryQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a new monthly summary", description = "Create a new monthly summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Monthly summary created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<MonthlySummaryResource> createMonthlySummary(@RequestBody CreateMonthlySummaryResource resource) {
        var command = CreateMonthlySummaryCommandFromResourceAssembler.toCommandFromResource(resource);
        var monthlySummaryId = commandService.handle(command);
        if (monthlySummaryId == null || monthlySummaryId == 0L) return ResponseEntity.badRequest().build();

        var query = new GetMonthlySummaryByIdQuery(monthlySummaryId);
        var result = queryService.handle(query);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = MonthlySummaryResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return new ResponseEntity<>(summaryResource, HttpStatus.CREATED);
    }

    @GetMapping("/{monthlySummaryId}")
    @Operation(summary = "Get monthly summary by ID", description = "Get a specific monthly summary by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summary found"),
            @ApiResponse(responseCode = "404", description = "Monthly summary not found")
    })
    public ResponseEntity<MonthlySummaryResource> getMonthlySummaryById(@PathVariable Long monthlySummaryId) {
        var query = new GetMonthlySummaryByIdQuery(monthlySummaryId);
        var result = queryService.handle(query);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = MonthlySummaryResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return ResponseEntity.ok(summaryResource);
    }

    @GetMapping
    @Operation(summary = "Get all monthly summaries", description = "Retrieve all existing monthly summaries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summaries found"),
            @ApiResponse(responseCode = "404", description = "No monthly summaries found")
    })
    public ResponseEntity<List<MonthlySummaryResource>> getAllMonthlySummaries() {
        var summaries = queryService.handle(new GetAllMonthlySummariesQuery());

        var resources = summaries.stream()
                .map(MonthlySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{monthlySummaryId}")
    @Operation(summary = "Update a monthly summary", description = "Update an existing monthly summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summary updated"),
            @ApiResponse(responseCode = "404", description = "Monthly summary not found")
    })
    public ResponseEntity<MonthlySummaryResource> updateMonthlySummary(@PathVariable Long monthlySummaryId, @RequestBody UpdateMonthlySummaryResource resource) {
        var command = UpdateMonthlySummaryCommandFromResourceAssembler.toCommandfromResource(monthlySummaryId, resource);
        var updated = commandService.handle(command);
        if (updated.isEmpty()) return ResponseEntity.notFound().build();

        var summaryResource = MonthlySummaryResourceFromEntityAssembler.toResourcefromEntity(updated.get());
        return ResponseEntity.ok(summaryResource);
    }

    @DeleteMapping("/{monthlySummaryId}")
    @Operation(summary = "Delete a monthly summary", description = "Delete an existing monthly summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summary deleted"),
            @ApiResponse(responseCode = "404", description = "Monthly summary not found")
    })
    public ResponseEntity<?> deleteMonthlySummary(@PathVariable Long monthlySummaryId) {
        commandService.handle(new DeleteMonthlySummaryCommand(monthlySummaryId));
        return ResponseEntity.ok("Monthly summary successfully deleted");
    }

    @GetMapping("/by-company/{companyId}")
    @Operation(summary = "Get monthly summaries by company ID", description = "Retrieve monthly summaries for a specific company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summaries found"),
            @ApiResponse(responseCode = "404", description = "No monthly summaries found for company")
    })
    public ResponseEntity<List<MonthlySummaryResource>> getMonthlySummariesByCompanyId(@PathVariable Long companyId) {
        var companyId_ = new CompanyId(companyId);
        var query = new GetMonthlySummariesByCompanyIdQuery(companyId_);
        var summaries = queryService.handle(query);

        var resources = summaries.stream()
                .map(MonthlySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/by-employee/{employeeId}")
    @Operation(summary = "Get monthly summaries by employee ID", description = "Retrieve monthly summaries for a specific employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly summaries found"),
            @ApiResponse(responseCode = "404", description = "No monthly summaries found for employee")
    })
    public ResponseEntity<List<MonthlySummaryResource>> getMonthlySummariesByEmployeeId(@PathVariable Long employeeId) {
        var employeeId_ = new EmployeeId(employeeId);
        var query = new GetMonthlySummariesByEmployeeIdQuery(employeeId_);
        var summaries = queryService.handle(query);

        var resources = summaries.stream()
                .map(MonthlySummaryResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
