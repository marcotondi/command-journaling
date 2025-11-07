package dev.marcotondi.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompositeCommand(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        List<Command<?>> commands
) implements Command<Void> {

    @Override
    public String commandType() {
        return CompositeCommand.class.getName();
    }
}
