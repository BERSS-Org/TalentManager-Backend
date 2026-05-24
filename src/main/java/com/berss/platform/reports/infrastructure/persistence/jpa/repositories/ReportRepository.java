package com.berss.platform.reports.infrastructure.persistence.jpa.repositories;

import com.berss.platform.reports.domain.model.aggregates.Report;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByTitle(String title);
    Optional<Report> findReportById(Long id);

    List<Report> findReportByCompanyId(CompanyId companyId);

    List<Report> findByEmployeeId(Long employeeId);
}
