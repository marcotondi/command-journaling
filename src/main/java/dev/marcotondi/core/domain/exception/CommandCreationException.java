package dev.marcotondi.core.domain.exception;

import java.util.Map;

public class CommandCreationException extends RuntimeException {
    private final String commandType;
    private final Map<String, Object> payload;

    public CommandCreationException(
            String message, String commandType, Map<String, Object> payload) {
        super(message);
        this.commandType = commandType;
        this.payload = payload;
    }

    public CommandCreationException(
            String message, String commandType, Map<String, Object> payload, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.payload = payload;
    }

    public String getCommandType() {
        return commandType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
