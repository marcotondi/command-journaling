package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.application.payload.CreateUserPayloadV1;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CommandTypeName;
import dev.marcotondi.domain.api.Payload;

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
