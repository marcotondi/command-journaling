package dev.marcotondi.domain.model;

public class CommandExecutedEvent {

    private final String commandId;
    private final String commandType;
    private final Object result;

    public CommandExecutedEvent(String commandId, String commandType, Object result) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.result = result;
    }

    public String getCommandId() {
        return commandId;
    }

    public String getCommandType() {
        return commandType;
    }

    public Object getResult() {
        return result;
    }
}