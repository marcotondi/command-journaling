package com.github.marcotondi.application.composite;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.core.UtilityCommand;
import com.github.marcotondi.core.domain.CompositeDescriptor;

/**
 * An example composite command that demonstrates how to use CommandComposite.
 * This command executes a 'sleep' operation followed by creating a 'todo' item.
 */
public class CreateUserCompositeDescriptor extends CompositeDescriptor {

    public CreateUserCompositeDescriptor() {
        super(CommandName.CREATE_USER_COMPOSITE, UtilityCommand.SYSTEM_ACTOR);
    }

    public CreateUserCompositeDescriptor(UUID commandId, LocalDateTime timestamp, String commandType, String actor) {
        super(commandId, timestamp, commandType, actor);
    }
}
