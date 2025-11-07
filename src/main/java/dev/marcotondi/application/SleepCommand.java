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
public record SleepCommand(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        int seconds) implements Command<String> {

    // Costruttore per deserializzazione JSON
    @JsonbCreator
    public SleepCommand(
            @JsonbProperty("commandId") UUID commandId,
            @JsonbProperty("timestamp") LocalDateTime timestamp,
            @JsonbProperty("actor") String actor,
            @JsonbProperty("commandType") String commandType,
            @JsonbProperty("email") int seconds) {
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
