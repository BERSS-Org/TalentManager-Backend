package com.berss.platform.iam.interfaces.rest;

import com.berss.platform.iam.domain.model.aggregates.User;
import com.berss.platform.iam.domain.services.UserCommandService;
import com.berss.platform.iam.domain.model.commands.CreateUserCommand;
import com.berss.platform.iam.interfaces.rest.resources.*;
import com.berss.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.berss.platform.iam.interfaces.rest.transform.CreateUserCommandFromResourceAssembler;
import com.berss.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import com.berss.platform.iam.interfaces.rest.transform.RegisterUserWithManagerCommandFromResourceAssembler;
import com.berss.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/auth/", produces = "application/json")
@Tag(name = "Authorization", description = "Identity and Access Management endpoints")
public class UsersController {

    private final UserCommandService commandService;

    public UsersController(UserCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user with username, password and managerId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserResource> createUser(@Valid @RequestBody CreateUserResource resource) {
        CreateUserCommand command = CreateUserCommandFromResourceAssembler.toCommandFromResource(resource);

        User user;
        try {
            user = commandService.handle(command);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (user == null || user.getId() == null || user.getId() <= 0)
            return ResponseEntity.badRequest().build();

        var userResource = new UserResource(
                user.getId(),
                user.getUsername(),
                user.getManagerId().getValue(),
                user.getCompanyId().companyId()
        );

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign-in with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = commandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authenticatedUser.get().getUser(),
                authenticatedUser.get().getToken()
        );
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/register-with-manager")
    @Operation(summary = "Register user and manager", description = "Creates a new manager and a new user if the manager doesn't exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User and manager created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserResource> registerWithManager(@Valid @RequestBody RegisterUserWithManagerResource resource) {
        var command = RegisterUserWithManagerCommandFromResourceAssembler.toCommandFromResource(resource);
        User user;
        try {
            user = commandService.handle(command);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        var userResource = new UserResource(
                user.getId(),
                user.getUsername(),
                user.getManagerId().getValue(),
                user.getCompanyId().companyId()
        );

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }
}
