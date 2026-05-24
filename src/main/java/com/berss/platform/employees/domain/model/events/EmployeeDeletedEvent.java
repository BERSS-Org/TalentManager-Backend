package com.berss.platform.employees.domain.model.events;

/**
 * Raised after an employee is removed so other bounded contexts (e.g. reports)
 * can clean up the data they anchored to that employee, instead of leaving
 * orphaned daily summaries and reports behind.
 */
public record EmployeeDeletedEvent(Long employeeId) {
}
