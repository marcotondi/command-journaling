package dev.marcotondi.application.todo.model;

import java.time.LocalDateTime;
import java.util.UUID;
import dev.marcotondi.application.CommandName;
import dev.marcotondi.core.domain.CommandDescriptor;

public class TodoDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 6671140159L;


    public TodoDescriptor() {
        super(CommandName.TODO_RC, "system");
    }

    public TodoDescriptor(UUID commandId, LocalDateTime timestamp, String commandType, String actor) {
        super(commandId, timestamp, commandType, actor);
    }

}
