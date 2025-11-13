package dev.marcotondi.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.application.payload.SleepPayloadV1;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CommandTypeName;
import dev.marcotondi.domain.api.Payload;

/**
 * A command to pause execution for a number of seconds.
 * This is an immutable data record that holds metadata and a payload.
 */
public record SleepDescriptor(
        UUID commandId,
        LocalDateTime timestamp,
        String actor,
        SleepPayloadV1 payload) implements CommandDescriptor {

    // Convenience constructor for creating a new command descriptor from scratch.
    public SleepDescriptor(String actor, int seconds) {
            this(UUID.randomUUID(),
                LocalDateTime.now(),
                actor,
                new SleepPayloadV1(seconds));
    }

    @Override
    public CommandTypeName commandType() {
        return CommandTypeName.SLEEP;
    }

    @Override
    public Payload getPayload() {
        return this.payload;
    }
}
