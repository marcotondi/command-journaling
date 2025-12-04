package dev.marcotondi.application.composite;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import dev.marcotondi.application.CommandName;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.domain.CommandComposite;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * An example composite command that demonstrates how to use CommandComposite.
 * This command executes a 'sleep' operation followed by creating a 'todo' item.
 */
@ApplicationScoped
@CommandType(CommandName.SIMPLE_COMPOSITE)
public class SimpleCompositeCommand extends CommandComposite<Void> {

    @Override
    public CommandDescriptor setDescriptor(
            Map<String, Object> payload,
            CommandDescriptor... commandDescriptors) {

        var descriptor = new SimpleCompositeDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                LocalDateTime.parse((String) payload.get("timestamp")),
                CommandName.SIMPLE_COMPOSITE,
                (String) payload.get("actor"),
                commandDescriptors);

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
