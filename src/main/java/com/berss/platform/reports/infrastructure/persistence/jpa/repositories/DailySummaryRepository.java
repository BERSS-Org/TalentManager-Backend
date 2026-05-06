package com.berss.platform.reports.infrastructure.persistence.jpa.repositories;

import com.berss.platform.reports.domain.model.aggregates.DailySummary;
import com.berss.platform.reports.domain.model.valueobjects.EmployeeId;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    Optional<DailySummary> findDailySummariesById(Long id);
    List<DailySummary> findDailySummariesByEmployeeId(EmployeeId employeeId);
    List<DailySummary> findDailySummariesByCompanyId(CompanyId companyId);

    @Query("""
            select d from DailySummary d
            where d.employeeId.employeeId = :employeeId
              and d.companyId.companyId = :companyId
              and d.specificDate.day = :day
              and d.specificDate.month = :month
              and d.specificDate.year = :year
            """)
    Optional<DailySummary> findByEmployeeCompanyAndDate(@Param("employeeId") Long employeeId,
                                                        @Param("companyId") Long companyId,
                                                        @Param("day") Integer day,
                                                        @Param("month") Integer month,
                                                        @Param("year") Integer year);

    boolean existsDailySummariesById(Long id);
}
