package com.berss.platform.reports.interfaces.rest;

import com.berss.platform.reports.domain.model.commands.CreateReportCommand;
import com.berss.platform.reports.domain.model.commands.DeleteReportCommand;
import com.berss.platform.reports.domain.model.commands.UpdateReportCommand;
import com.berss.platform.reports.domain.model.queries.GetAllReportsQuery;
import com.berss.platform.reports.domain.model.queries.GetReportByIdQuery;
import com.berss.platform.reports.domain.model.queries.GetReportsByCompanyIdQuery;
import com.berss.platform.reports.domain.services.ReportCommandService;
import com.berss.platform.reports.domain.services.ReportQueryService;
import com.berss.platform.reports.interfaces.rest.resources.CreateReportResource;
import com.berss.platform.reports.interfaces.rest.resources.ReportResource;
import com.berss.platform.reports.interfaces.rest.resources.UpdateReportResource;
import com.berss.platform.reports.interfaces.rest.transform.CreateReportCommandFromResourceAssembler;
import com.berss.platform.reports.interfaces.rest.transform.ReportResourceFromEntityAssembler;
import com.berss.platform.reports.interfaces.rest.transform.UpdateReportCommandFromResourceAssembler;

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

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/reports", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reports", description = "Available Report Endpoints")
public class ReportsController {

    private final ReportCommandService commandService;
    private final ReportQueryService queryService;

    public ReportsController(ReportCommandService commandService, ReportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a new report", description = "Create a new report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ReportResource> createReport(@RequestBody CreateReportResource resource) {
        CreateReportCommand command = CreateReportCommandFromResourceAssembler.toCommandFromResource(resource);
        Long reportId = commandService.handle(command);
        if (reportId == null || reportId == 0L) return ResponseEntity.badRequest().build();

        var result = queryService.handle(new GetReportByIdQuery(reportId));
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var reportResource = ReportResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return new ResponseEntity<>(reportResource, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID", description = "Get a specific report by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report found"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<ReportResource> getReportById(@PathVariable Long reportId) {
        var result = queryService.handle(new GetReportByIdQuery(reportId));
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var reportResource = ReportResourceFromEntityAssembler.toResourcefromEntity(result.get());
        return ResponseEntity.ok(reportResource);
    }

    @GetMapping
    @Operation(summary = "Get all reports", description = "Retrieve all existing reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports found"),
            @ApiResponse(responseCode = "404", description = "No reports found")
    })
    public ResponseEntity<List<ReportResource>> getAllReports() {
        var reports = queryService.handle(new GetAllReportsQuery());

        var resources = reports.stream()
                .map(ReportResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update a report", description = "Update an existing report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report updated"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<ReportResource> updateReport(@PathVariable Long reportId, @RequestBody UpdateReportResource resource) {
        UpdateReportCommand command = UpdateReportCommandFromResourceAssembler.toCommandfromResource(reportId, resource);
        var updated = commandService.handle(command);
        if (updated.isEmpty()) return ResponseEntity.notFound().build();

        var reportResource = ReportResourceFromEntityAssembler.toResourcefromEntity(updated.get());
        return ResponseEntity.ok(reportResource);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a report", description = "Delete an existing report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report deleted"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        commandService.handle(new DeleteReportCommand(reportId));
        return ResponseEntity.ok("Report successfully deleted");
    }

    @GetMapping("/by-company/{companyId}")
    @Operation(summary = "Get reports by company ID", description = "Retrieve reports for a specific company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports found"),
            @ApiResponse(responseCode = "404", description = "No reports found for company")
    })
    public ResponseEntity<List<ReportResource>> getReportsByCompanyId(@PathVariable Long companyId) {
        var reports = queryService.handle(new GetReportsByCompanyIdQuery(new CompanyId(companyId)));

        var resources = reports.stream()
                .map(ReportResourceFromEntityAssembler::toResourcefromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
