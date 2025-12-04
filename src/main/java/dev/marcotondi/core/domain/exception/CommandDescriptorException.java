package dev.marcotondi.core.domain.exception;

public class CommandDescriptorException extends RuntimeException {
    private final String commandType;
    private final String payload;

    public CommandDescriptorException(
            String message, String commandType, String payload) {
        super(message);
        this.commandType = commandType;
        this.payload = payload;
    }

    public String getCommandType() {
        return commandType;
    }

    public String getPayload() {
        return payload;
    }
}
