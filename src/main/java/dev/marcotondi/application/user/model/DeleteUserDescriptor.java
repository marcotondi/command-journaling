package dev.marcotondi.application.user.model;

import java.time.LocalDateTime;
import java.util.UUID;
import dev.marcotondi.application.CommandName;
import dev.marcotondi.core.domain.CommandDescriptor;

/**
 * A command to delete a user.
 * This is an immutable data record that holds metadata and a payload.
 */
public class DeleteUserDescriptor extends CommandDescriptor {

    private final String email;

    public DeleteUserDescriptor(String actor, String email) {
        super(CommandName.DELETE_USER, actor);

        this.email = email;
    }

    public DeleteUserDescriptor(UUID commandId, LocalDateTime timestamp, String commandType, String actor,
            String email) {
        super(commandId, timestamp, commandType, actor);

        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "{" +
                " commandId='" + getCommandId() + "'" +
                ", commandType='" + getCommandType() + "'" +
                ", timestamp='" + getTimestamp() + "'" +
                ", actor='" + getActor() + "'" +
                ", email='" + getEmail() + "'" +
                "}";
    }
}
