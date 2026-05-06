package com.berss.platform.iam.interfaces.rest;

import com.berss.platform.business.infrastructure.persistence.jpa.repositories.CompanyQueryRepository;
import com.berss.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.berss.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.berss.platform.iam.interfaces.rest.resources.ChangePasswordResource;
import com.berss.platform.iam.interfaces.rest.resources.ProfileResource;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Profile endpoints — they always operate on the authenticated user.
 * Avoids passing userId in the path so users can never read or modify
 * other users' accounts.
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/profile", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Profile", description = "Authenticated user profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final CompanyQueryRepository companyRepository;
    private final HashingService hashingService;

    public ProfileController(UserRepository userRepository,
                             CompanyQueryRepository companyRepository,
                             HashingService hashingService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.hashingService = hashingService;
    }

    @GetMapping
    public ResponseEntity<ProfileResource> getProfile() {
        var user = currentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var company = companyRepository.findById(user.getCompanyId().getValue()).orElse(null);
        return ResponseEntity.ok(new ProfileResource(
                user.getId(),
                user.getUsername(),
                user.getManagerId().getValue(),
                user.getCompanyId().getValue(),
                company != null ? company.getName() : null,
                user.getCreatedAt()
        ));
    }

    @PatchMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordResource payload) {
        var user = currentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!hashingService.matches(payload.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_CURRENT_PASSWORD");
        }

        user.changePassword(hashingService.encode(payload.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    /** Pulls the username from Spring Security and resolves the User entity. */
    private com.berss.platform.iam.domain.model.aggregates.User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
