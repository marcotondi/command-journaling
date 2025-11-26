package dev.marcotondi.application.todo.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.CommandDescriptor;

public class TodoDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 6671140159L;


    public TodoDescriptor() {
        super(CommandTypeName.TODO, "system");
    }

    public TodoDescriptor(UUID commandId, LocalDateTime timestamp, CommandTypeName commandType, String actor) {
        super(commandId, timestamp, commandType, actor);
    }

}
