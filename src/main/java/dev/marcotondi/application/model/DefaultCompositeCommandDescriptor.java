package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CompositeCommandDescriptor;

public record DefaultCompositeCommandDescriptor(
    UUID commandId,
    LocalDateTime timestamp,
    String actor,
    List<CommandDescriptor<?>> commands
) implements CompositeCommandDescriptor<List<CommandDescriptor<?>>> {

    @Override
    public String commandType() {
        return DefaultCompositeCommandDescriptor.class.getName();
    }
}
