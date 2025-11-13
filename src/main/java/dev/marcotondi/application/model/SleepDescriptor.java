package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import dev.marcotondi.domain.api.CommandDescriptor;

/**
 * A command to create a new user.
 * This is an immutable data record.
 */
public record SleepDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        String commandType,
        int seconds) implements CommandDescriptor {

    // Costruttore per deserializzazione JSON
    @JsonCreator
    public SleepDescriptor(
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

    public SleepDescriptor(String actor, int seconds) {
        this(UUID.randomUUID(), LocalDateTime.now(), actor, "Sleep", seconds);
    }
}
