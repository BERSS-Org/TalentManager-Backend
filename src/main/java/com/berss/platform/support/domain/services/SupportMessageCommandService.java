package com.berss.platform.support.domain.services;

import com.berss.platform.support.domain.model.aggregates.SupportMessage;
import com.berss.platform.support.domain.model.commands.ChangeStatusCommand;
import com.berss.platform.support.domain.model.commands.CreateSupportMessageCommand;
import com.berss.platform.support.domain.model.commands.DeleteSupportMessageCommand;
import com.berss.platform.support.domain.model.commands.UpdateSupportMessageCommand;

import java.util.Optional;

public interface SupportMessageCommandService {

    //Crear
    Long handle(CreateSupportMessageCommand command);
    //Actualizar
    Optional<SupportMessage> handle(ChangeStatusCommand command);

    Optional<SupportMessage> handle(UpdateSupportMessageCommand command);

    //Borrar
    void handle(DeleteSupportMessageCommand command);

}
