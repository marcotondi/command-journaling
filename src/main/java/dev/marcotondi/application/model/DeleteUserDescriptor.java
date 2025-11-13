package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.application.payload.DeleteUserPayloadV1;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CommandTypeName;
import dev.marcotondi.domain.api.Payload;

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
