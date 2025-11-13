package dev.marcotondi.application.exception;

import java.util.Map;

public class CompositeCommandCreationException extends RuntimeException {
    private final String commandType;
    private final Map<String, Object> payload;

    public CompositeCommandCreationException(
            String message, String commandType, Map<String, Object> payload) {
        super(message);
        this.commandType = commandType;
        this.payload = payload;
    }

    public CompositeCommandCreationException(
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
