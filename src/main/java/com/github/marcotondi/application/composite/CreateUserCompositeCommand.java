package com.github.marcotondi.application.composite;

import static com.github.marcotondi.core.UtilityCommand.extractOrDefaultActor;
import static com.github.marcotondi.core.UtilityCommand.extractOrGenerateTimestamp;
import static com.github.marcotondi.core.UtilityCommand.extractOrGenerateUuid;

import java.util.Map;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.api.Commands;
import com.github.marcotondi.core.domain.CommandComposite;
import com.github.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * An example composite command that demonstrates how to use CommandComposite.
 * This command executes a 'sleep' operation followed by creating a 'todo' item.
 */
@ApplicationScoped
@CommandType(CommandName.CREATE_USER_COMPOSITE)
@Commands(commandTypes = { CommandName.CREATE_USER, CommandName.SLEEP })
public class CreateUserCompositeCommand extends CommandComposite<Void> {

    @Override
    public CommandDescriptor setDescriptor(
            Map<String, Object> payload) {

        var descriptor = new CreateUserCompositeDescriptor(
                extractOrGenerateUuid(payload),
                extractOrGenerateTimestamp(payload),
                CommandName.CREATE_USER_COMPOSITE,
                extractOrDefaultActor(payload));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
