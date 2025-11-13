package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import dev.marcotondi.domain.api.CommandDescriptor;

/**
 * A command to create a new user.
 * This is an immutable data record.
 */
public record CreateUserDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        String username,
        String email) implements CommandDescriptor {

    // Costruttore per deserializzazione JSON
    @JsonCreator
    public CreateUserDescriptor(
            UUID commandId,
            LocalDateTime timestamp,
            String actor,
            String commandType,
            String username,
            String email) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.commandType = commandType;
        this.username = username;
        this.email = email;
    }

    public CreateUserDescriptor(String actor, String username, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, "CreateUser", username, email);
    }
}
