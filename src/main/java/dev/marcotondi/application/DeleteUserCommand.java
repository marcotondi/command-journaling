package dev.marcotondi.application;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import dev.marcotondi.domain.model.Command;

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
    @JsonCreator
    public DeleteUserCommand(
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

    public DeleteUserCommand(String actor, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, DeleteUserCommand.class.getName(), email);
    }
}
