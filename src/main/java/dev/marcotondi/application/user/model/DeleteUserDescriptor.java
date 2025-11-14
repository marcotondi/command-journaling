package dev.marcotondi.application.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.Payload;

/**
 * A command to delete a user.
 * This is an immutable data record that holds metadata and a payload.
 */
public record DeleteUserDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        DeleteUserPayloadV1 payload) implements CommandDescriptor {

    // Convenience constructor for creating a new command descriptor from scratch.
    public DeleteUserDescriptor(String actor, String email) {
        this(UUID.randomUUID(),
                LocalDateTime.now(),
                actor,
                new DeleteUserPayloadV1(email));
    }

    @Override
    public CommandTypeName commandType() {
        return CommandTypeName.DELETE_USER;
    }

    @Override
    public Payload getPayload() {
        return this.payload;
    }
}
