package com.berss.platform.support.application.internal.commandservices;

import com.berss.platform.support.domain.model.aggregates.SupportMessage;
import com.berss.platform.support.domain.model.commands.*;
import com.berss.platform.support.domain.model.entities.Status;
import com.berss.platform.support.domain.model.valueobjects.SupportStatus;
import com.berss.platform.support.domain.services.SupportMessageCommandService;
import com.berss.platform.support.infrastructure.persistence.jpa.repositories.StatusRepository;
import com.berss.platform.support.infrastructure.persistence.jpa.repositories.SupportMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SupportMessageCommandServiceImpl implements SupportMessageCommandService {
    private final SupportMessageRepository supportMessageRepository;
    private final StatusRepository statusRepository;

    public SupportMessageCommandServiceImpl(SupportMessageRepository supportMessageRepository, StatusRepository statusRepository) {
        this.supportMessageRepository = supportMessageRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public Long handle(CreateSupportMessageCommand command) {
        if (supportMessageRepository.existsByContent(command.content())) {
            throw new IllegalArgumentException("Support message with content '%s' already exists".formatted(command.content()));
        }

        var pendingStatus = statusRepository.findByName(SupportStatus.PENDING)
                .orElseGet(() -> statusRepository.save(new Status(SupportStatus.PENDING)));
        var supportMessage = new SupportMessage(command, pendingStatus);

        try {
            supportMessageRepository.save(supportMessage);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving support message: %s".formatted(e.getMessage()));
        }

        return supportMessage.getId();
    }

    @Override
    public Optional<SupportMessage> handle(UpdateSupportMessageCommand command) {
        var supportMessage = supportMessageRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("SupportMessage with id %d not found".formatted(command.id())));

        supportMessage.updateInformation(command);

        try {
            supportMessageRepository.save(supportMessage);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving updated support message: %s".formatted(e.getMessage()));
        }

        return Optional.of(supportMessage);
    }

    @Override
    public Optional<SupportMessage> handle(ChangeStatusCommand command) {
        var messageId = command.supportMessageId();
        var newStatusName = command.newStatus();

        var supportMessage = supportMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("SupportMessage with id %d not found".formatted(messageId)));

        var supportStatus = SupportStatus.valueOf(newStatusName.trim().toUpperCase());
        var status = statusRepository.findByName(supportStatus)
                .orElseGet(() -> statusRepository.save(new Status(supportStatus)));

        supportMessage.changeStatus(status);

        var updated = supportMessageRepository.save(supportMessage);
        return Optional.of(updated);
    }

    @Override
    public void handle(DeleteSupportMessageCommand command) {
        if (!supportMessageRepository.existsById(command.supportMessageId())) {
            throw new IllegalArgumentException("SupportMessage with id %s not found".formatted(command.supportMessageId()));
        }

        try {
            supportMessageRepository.deleteById(command.supportMessageId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting SupportMessage: %s".formatted(e.getMessage()));
        }
    }
}
