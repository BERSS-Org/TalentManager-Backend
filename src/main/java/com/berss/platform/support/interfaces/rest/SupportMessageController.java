package com.berss.platform.support.interfaces.rest;

import com.berss.platform.support.domain.model.commands.CreateSupportMessageCommand;
import com.berss.platform.support.domain.model.commands.ChangeStatusCommand;
import com.berss.platform.support.domain.model.commands.DeleteSupportMessageCommand;
import com.berss.platform.support.domain.model.commands.UpdateSupportMessageCommand;
import com.berss.platform.support.domain.model.queries.GetAllSupportMessagesQuery;
import com.berss.platform.support.domain.model.queries.GetSupportMessageByIdQuery;
import com.berss.platform.support.domain.model.queries.GetSupportMessagesByCompanyIdQuery;
import com.berss.platform.support.domain.services.SupportMessageCommandService;
import com.berss.platform.support.domain.services.SupportMessageQueryService;
import com.berss.platform.support.interfaces.rest.resources.CreateSupportMessageResource;
import com.berss.platform.support.interfaces.rest.resources.ChangeStatusResource;
import com.berss.platform.support.interfaces.rest.resources.SupportMessageResource;
import com.berss.platform.support.interfaces.rest.resources.UpdateSupportMessageResource;
import com.berss.platform.support.interfaces.rest.transform.CreateSupportMessageCommandFromResourceAssembler;
import com.berss.platform.support.interfaces.rest.transform.SupportMessageResourceFromEntityAssembler;
import com.berss.platform.support.interfaces.rest.transform.UpdateSupportMessageCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/support-messages", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Support Messages", description = "Support message endpoints")
public class SupportMessageController {
    private final SupportMessageCommandService commandService;
    private final SupportMessageQueryService queryService;

    public SupportMessageController(SupportMessageCommandService commandService, SupportMessageQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a support message")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Support message created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupportMessageResource> createSupportMessage(@RequestBody CreateSupportMessageResource resource) {
        var command = CreateSupportMessageCommandFromResourceAssembler.toCommandFromResource(resource);
        var id = commandService.handle(command);
        var message = queryService.handle(new GetSupportMessageByIdQuery(id));
        return message
                .map(m -> new ResponseEntity<>(SupportMessageResourceFromEntityAssembler.toResourceFromEntity(m), HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    @Operation(summary = "Get all support messages")
    public ResponseEntity<List<SupportMessageResource>> getAllSupportMessages() {
        var messages = queryService.handle(new GetAllSupportMessagesQuery());
        var resources = messages.stream()
                .map(SupportMessageResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get support message by ID")
    public ResponseEntity<SupportMessageResource> getSupportMessageById(@PathVariable Long id) {
        var message = queryService.handle(new GetSupportMessageByIdQuery(id));
        return message
                .map(m -> ResponseEntity.ok(SupportMessageResourceFromEntityAssembler.toResourceFromEntity(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get support messages by company ID")
    public ResponseEntity<List<SupportMessageResource>> getSupportMessagesByCompanyId(@PathVariable Long companyId) {
        var messages = queryService.handle(new GetSupportMessagesByCompanyIdQuery(companyId));
        var resources = messages.stream()
                .map(SupportMessageResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a support message")
    public ResponseEntity<SupportMessageResource> updateSupportMessage(@PathVariable Long id, @RequestBody UpdateSupportMessageResource resource) {
        var command = UpdateSupportMessageCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var updated = commandService.handle(command);
        return updated
                .map(m -> ResponseEntity.ok(SupportMessageResourceFromEntityAssembler.toResourceFromEntity(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change support message status")
    public ResponseEntity<SupportMessageResource> changeSupportMessageStatus(@PathVariable Long id, @RequestBody ChangeStatusResource resource) {
        var updated = commandService.handle(new ChangeStatusCommand(id, resource.newStatus()));
        return updated
                .map(m -> ResponseEntity.ok(SupportMessageResourceFromEntityAssembler.toResourceFromEntity(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a support message")
    public ResponseEntity<?> deleteSupportMessage(@PathVariable Long id) {
        commandService.handle(new DeleteSupportMessageCommand(id));
        return ResponseEntity.ok("Support message deleted");
    }
}
