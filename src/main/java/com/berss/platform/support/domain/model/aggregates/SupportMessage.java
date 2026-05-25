package com.berss.platform.support.domain.model.aggregates;

import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import com.berss.platform.support.domain.model.commands.CreateSupportMessageCommand;
import com.berss.platform.support.domain.model.commands.UpdateSupportMessageCommand;
import com.berss.platform.support.domain.model.entities.Status;
import com.berss.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.berss.platform.support.domain.model.valueobjects.ReceivedAt;
import com.berss.platform.support.domain.model.valueobjects.RequestDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
public class SupportMessage extends AuditableAbstractAggregateRoot<SupportMessage> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 500)
    private String content;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    })
    private CompanyId companyId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "request_date"))
    })
    private RequestDate requestDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "received_at"))
    })
    private ReceivedAt receivedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private Status status;

    public SupportMessage() {
        // Constructor por defecto requerido por JPA
    }

    // Constructor usado al crear un mensaje: puede recibir status como null
    public SupportMessage(CreateSupportMessageCommand command, Status status) {
        this.content = command.content();
        this.companyId = new CompanyId(command.companyId());
        this.requestDate = new RequestDate(command.requestDate());
        this.receivedAt = new ReceivedAt(command.receivedAt());
        this.status = status; // puede ser null
    }

    // Constructor usado al actualizar: también recibe Status (puede ser null si no se cambia)
    public SupportMessage(UpdateSupportMessageCommand command, Status status) {
        if (command.id() != null && command.id() > 0) {
            this.id = command.id();
        }
        this.content = command.content();
        this.companyId = new CompanyId(command.companyId());
        this.status = status;
    }

    // Constructor adicional para uso opcional (como pruebas / seeding)
    public SupportMessage(String content, Long companyId, Instant requestDate, Instant receivedAt, Status status) {
        this.content = content;
        this.companyId = new CompanyId(companyId);
        this.requestDate = new RequestDate(requestDate);
        this.receivedAt = new ReceivedAt(receivedAt);
        this.status = status;
    }

    public void changeStatus(Status newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status cannot be null");
        this.status = newStatus;
    }

    public void updateInformation(UpdateSupportMessageCommand command) {
        this.content = command.content();
        this.companyId = new CompanyId(command.companyId());
    }

    public String getStatusAsString() {
        return status != null ? status.getStringName() : "PENDING";
    }
}
