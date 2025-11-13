package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CommandTypeName;
import dev.marcotondi.domain.api.Payload;

public record CompositeCommandDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        List<CommandDescriptor> childCommands) implements CommandDescriptor {

    // Convenience constructor
    public CompositeCommandDescriptor(
            String actor,
            List<CommandDescriptor> childCommands) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, childCommands);
    }

    @Override
    public CommandTypeName commandType() {
        return CommandTypeName.COMPOSITE_COMMAND;
    }

    @Override
    public Payload getPayload() {
        // A composite command's payload is represented by its children,
        // which are journaled individually. It has no intrinsic payload itself.
        return null;
    }

    // Metodo helper per creare un descrittore da una lista di comandi
    public static CompositeCommandDescriptor fromCommands(
            String actor,
            List<? extends Command<?>> commands) {
        List<CommandDescriptor> childDescriptors = commands.stream()
                .map(Command::getDescriptor)
                .toList();
        return new CompositeCommandDescriptor(
                actor,
                childDescriptors);
    }
}
