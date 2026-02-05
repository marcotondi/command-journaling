package com.github.marcotondi.application.todo.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.core.UtilityCommand;
import com.github.marcotondi.core.domain.CommandDescriptor;

public class TodoDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 6671140159L;


    public TodoDescriptor() {
        super(CommandName.TODO_RC, UtilityCommand.SYSTEM_ACTOR);
    }

    public TodoDescriptor(UUID commandId, LocalDateTime timestamp, String commandType, String actor) {
        super(commandId, timestamp, commandType, actor);
    }

}
