package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import dev.marcotondi.domain.api.CommandDescriptor;

/**
 * A command to create a new user.
 * This is an immutable data record.
 */
public record DeleteUserDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        String email) implements CommandDescriptor {

    // Costruttore per deserializzazione JSON
    @JsonCreator
    public DeleteUserDescriptor(
            UUID commandId,
            LocalDateTime timestamp,
            String actor,
            String commandType,
            String email) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.commandType = commandType;
        this.email = email;
    }

    public DeleteUserDescriptor(String actor, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, "DeleteUser", email);
    }
}
