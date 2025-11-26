package dev.marcotondi.application.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.CommandDescriptor;

public class CreateUserDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 8583766018L;

    private final String username;
    private final String email;

    public CreateUserDescriptor(String actor, String username, String email) {
        super(CommandTypeName.CREATE_USER, actor);

        this.username = username;
        this.email = email;
    }

    public CreateUserDescriptor(UUID commandId, LocalDateTime timestamp, CommandTypeName commandType, String actor, String username, String email) {
        super(commandId, timestamp, commandType, actor);

        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
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
                ", username='" + getUsername() + "'" +
                ", email='" + getEmail() + "'" +
                "}";
    }

}
