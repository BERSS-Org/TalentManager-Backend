package com.berss.platform.iam.domain.model.aggregates;

import com.berss.platform.shared.domain.model.entities.AuditableModel;
import com.berss.platform.iam.domain.model.valueobjects.ManagerId;

import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "users")
public class User extends AuditableModel {

    @Getter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Column(nullable = false)
    private String password;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "manager_id"))
    @Getter
    private ManagerId managerId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    @Getter
    private CompanyId companyId;

    protected User() {
    }

    public User(String username, String hashedPassword, Long managerId, Long companyId) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username is required.");
        if (hashedPassword == null || hashedPassword.isBlank())
            throw new IllegalArgumentException("Password is required.");
        if (managerId == null)
            throw new IllegalArgumentException("Manager Id is required.");
        if (companyId == null)
            throw new IllegalArgumentException("Company Id is required.");

        this.username = username;
        this.password = hashedPassword;
        this.managerId = new ManagerId(managerId);
        this.companyId = new CompanyId(companyId);
    }

    public void changePassword(String newHashedPassword) {
        if (newHashedPassword == null || newHashedPassword.isBlank())
            throw new IllegalArgumentException("Password is required.");
        this.password = newHashedPassword;
    }
}
