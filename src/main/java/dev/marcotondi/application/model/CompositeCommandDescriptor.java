package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;

public record CompositeCommandDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        List<CommandDescriptor> childCommands
) implements CommandDescriptor {

    // Constructor che genera automaticamente l'ID se non fornito
    public CompositeCommandDescriptor(
            LocalDateTime timestamp,
            String actor,
            String commandType,
            List<CommandDescriptor> childCommands) {
        this(UUID.randomUUID(), timestamp, actor, commandType, childCommands);
    }

    // Metodo helper per creare un descrittore da una lista di comandi
    public static CompositeCommandDescriptor fromCommands(
            String actor,
            String commandType,
            List<? extends Command<?>> commands) {
        List<CommandDescriptor> childDescriptors = commands.stream()
            .map(Command::getDescriptor)
            .toList();
        return new CompositeCommandDescriptor(
            LocalDateTime.now(),
            actor,
            commandType,
            childDescriptors
        );
    }
}
