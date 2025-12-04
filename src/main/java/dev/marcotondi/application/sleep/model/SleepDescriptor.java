package dev.marcotondi.application.sleep.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.CommandDescriptor;

/**
 * A command to pause execution for a number of seconds.
 * This is an immutable data record that holds metadata and a payload.
 */
public class SleepDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 4483220038L;

    private final int seconds;

    public SleepDescriptor(String actor, int seconds) {
        super(CommandTypeName.SLEEP, actor);

        this.seconds = seconds;
    }

    public SleepDescriptor(UUID commandId, LocalDateTime timestamp, CommandTypeName commandType, String actor, int seconds) {
        super(commandId, timestamp, commandType, actor);

        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public String toString() {
        return "{" +
                " commandId='" + getCommandId() + "'" +
                ", commandType='" + getCommandType() + "'" +
                ", timestamp='" + getTimestamp() + "'" +
                ", actor='" + getActor() + "'" +
                ", seconds='" + getSeconds() + "'" +
                "}";
    }

}
