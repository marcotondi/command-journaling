package dev.marcotondi.application;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import dev.marcotondi.domain.model.Command;

/**
 * A command to create a new user.
 * This is an immutable data record.
 */
public record SleepCommand(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        int seconds) implements Command<String> {

    // Costruttore per deserializzazione JSON
    @JsonCreator
    public SleepCommand(
            UUID commandId,
            LocalDateTime timestamp,
            String actor,
            String commandType,
            int seconds) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.commandType = commandType;
        this.seconds = seconds;
    }

    public SleepCommand(String actor, int seconds) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, SleepCommand.class.getName(), seconds);
    }
}
