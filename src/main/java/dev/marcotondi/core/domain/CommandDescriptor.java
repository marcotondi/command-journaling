package dev.marcotondi.core.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandTypeName;

/**
 * Base interface for all commands.
 * A command is an immutable data structure that represents a request to perform
 * an action.
 */
public abstract class CommandDescriptor implements Serializable {

    public static final int version = 1;

    private final UUID commandId;
    private final LocalDateTime timestamp;
    private final CommandTypeName commandType;
    private final String actor;

    public CommandDescriptor(CommandTypeName commandType, String actor) {
        this.commandId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.commandType = commandType;
        this.actor = actor;
    }

    public CommandDescriptor(UUID commandId, LocalDateTime timestamp, CommandTypeName commandType, String actor) {
        this.commandId = commandId;
        this.timestamp = timestamp;
        this.commandType = commandType;
        this.actor = actor;
    }

    public String getCommandId() {
        return this.commandId.toString();
    }

    public CommandTypeName getCommandType() {
        return this.commandType;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getActor() {
        return this.actor;
    }

    @Override
    public String toString() {
        return "{" +
                " commandId='" + getCommandId() + "'" +
                ", commandType='" + getCommandType() + "'" +
                ", timestamp='" + getTimestamp() + "'" +
                ", actor='" + getActor() + "'" +
                "}";
    }

}
