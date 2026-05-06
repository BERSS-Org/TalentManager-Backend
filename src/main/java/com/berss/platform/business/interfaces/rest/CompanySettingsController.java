package com.berss.platform.business.interfaces.rest;

import com.berss.platform.business.domain.model.entities.Company;
import com.berss.platform.business.infrastructure.persistence.jpa.repositories.CompanyQueryRepository;
import com.berss.platform.business.interfaces.rest.resources.CompanySettingsResource;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/companies", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Company Settings", description = "Workspace-wide operating defaults")
public class CompanySettingsController {

    private final CompanyQueryRepository companyRepository;

    public CompanySettingsController(CompanyQueryRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/{companyId}/settings")
    public ResponseEntity<CompanySettingsResource> getSettings(@PathVariable Long companyId) {
        return companyRepository.findById(companyId)
                .map(this::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{companyId}/settings")
    @Transactional
    public ResponseEntity<CompanySettingsResource> updateSettings(@PathVariable Long companyId,
                                                                   @RequestBody CompanySettingsResource payload) {
        var maybeCompany = companyRepository.findById(companyId);
        if (maybeCompany.isEmpty()) return ResponseEntity.notFound().build();
        var company = maybeCompany.get();

        if (payload.expectedDailyHours() != null && payload.expectedDailyHours() > 0 && payload.expectedDailyHours() <= 24) {
            company.setExpectedDailyHours(payload.expectedDailyHours());
        }
        if (payload.workingDaysPerMonth() != null && payload.workingDaysPerMonth() > 0 && payload.workingDaysPerMonth() <= 31) {
            company.setWorkingDaysPerMonth(payload.workingDaysPerMonth());
        }
        if (payload.currencyCode() != null && !payload.currencyCode().isBlank()) {
            company.setCurrencyCode(payload.currencyCode().trim().toUpperCase());
        }
        if (payload.defaultHourlyRate() != null && payload.defaultHourlyRate() >= 0) {
            company.setDefaultHourlyRate(payload.defaultHourlyRate());
        }
        if (payload.defaultHourlyCost() != null && payload.defaultHourlyCost() >= 0) {
            company.setDefaultHourlyCost(payload.defaultHourlyCost());
        }

        companyRepository.save(company);
        return ResponseEntity.ok(toResource(company));
    }

    private CompanySettingsResource toResource(Company company) {
        return new CompanySettingsResource(
                company.getId(),
                company.getName(),
                company.getCurrencyCodeOrDefault(),
                company.getExpectedDailyHoursOrDefault(),
                company.getWorkingDaysPerMonthOrDefault(),
                company.getDefaultHourlyRateOrZero(),
                company.getDefaultHourlyCostOrZero()
        );
    }
}
