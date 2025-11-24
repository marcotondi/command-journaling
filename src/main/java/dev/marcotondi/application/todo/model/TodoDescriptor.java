package dev.marcotondi.application.todo.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.CommandDescriptor;

public class TodoDescriptor extends CommandDescriptor {

    public TodoDescriptor() {
        super(CommandTypeName.TODO, "system");
    }

    public TodoDescriptor(UUID commandId, LocalDateTime timestamp, CommandTypeName commandType, String actor) {
        super(commandId, timestamp, commandType, actor);
    }

}
