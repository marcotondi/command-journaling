package dev.marcotondi.application;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.domain.model.Command;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * A command to create a new user.
 * This is an immutable data record.
 */
public record DeleteUserCommand(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        String email) implements Command<String> {

    // Costruttore per deserializzazione JSON
    @JsonbCreator
    public DeleteUserCommand(
            @JsonbProperty("commandId") UUID commandId,
            @JsonbProperty("timestamp") LocalDateTime timestamp,
            @JsonbProperty("actor") String actor,
            @JsonbProperty("commandType") String commandType,
            @JsonbProperty("email") String email) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.commandType = commandType;
        this.email = email;
    }

    public DeleteUserCommand(String actor, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, DeleteUserCommand.class.getName(), email);
    }
}
