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
public record CreateUserCommand(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        String username,
        String email) implements Command<String> {

    // Costruttore per deserializzazione JSON
    @JsonbCreator
    public CreateUserCommand(
            @JsonbProperty("commandId") UUID commandId,
            @JsonbProperty("timestamp") LocalDateTime timestamp,
            @JsonbProperty("actor") String actor,
            @JsonbProperty("commandType") String commandType,
            @JsonbProperty("username") String username,
            @JsonbProperty("email") String email) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.commandType = commandType;
        this.username = username;
        this.email = email;
    }

    public CreateUserCommand(String actor, String username, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, CreateUserCommand.class.getName(), username, email);
    }
}
