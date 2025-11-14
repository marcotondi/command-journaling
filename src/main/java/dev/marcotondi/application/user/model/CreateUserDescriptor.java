package dev.marcotondi.application.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.Payload;

/**
 * A command to create a new user.
 * This is an immutable data record that holds metadata and a payload.
 */
public record CreateUserDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        CreateUserPayloadV1 payload) implements CommandDescriptor {

    // Convenience constructor for creating a new command descriptor from scratch.
    public CreateUserDescriptor(String actor, String username, String email) {
        this(UUID.randomUUID(),
                LocalDateTime.now(),
                actor,
                new CreateUserPayloadV1(username, email));
    }

    @Override
    public CommandTypeName commandType() {
        return CommandTypeName.CREATE_USER;
    }

    @Override
    public Payload getPayload() {
        return this.payload;
    }
}
