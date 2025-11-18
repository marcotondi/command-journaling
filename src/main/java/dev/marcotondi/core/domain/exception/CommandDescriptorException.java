package dev.marcotondi.core.domain.exception;

import dev.marcotondi.core.api.CommandTypeName;

public class CommandDescriptorException extends RuntimeException {
    private final String commandType;
    private final String payload;

    public CommandDescriptorException(
            String message, CommandTypeName commandType, String payload) {
        super(message);
        this.commandType = commandType.name();
        this.payload = payload;
    }

    public String getCommandType() {
        return commandType;
    }

    public String getPayload() {
        return payload;
    }
}
