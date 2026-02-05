package com.github.marcotondi.core.domain.exception;

public class CommandNotFoundException extends RuntimeException {

    private final String commandType;

    /**
     * Creates a new CommandNotFoundException for the specified command type.
     *
     * @param commandType the command type that could not be found
     */
    public CommandNotFoundException(String commandType) {
        super(String.format(
                "No command found for type: '%s'. Ensure the command class is annotated with @CommandType(\"%s\") and is a CDI bean.",
                commandType, commandType));
        this.commandType = commandType;
    }

    /**
     * Creates a new CommandNotFoundException with a custom message.
     *
     * @param commandType the command type that could not be found
     * @param message     additional context about the failure
     */
    public CommandNotFoundException(String commandType, String message) {
        super(String.format("Command type '%s' not found: %s", commandType, message));
        this.commandType = commandType;
    }

    /**
     * Returns the command type that could not be resolved.
     *
     * @return the missing command type
     */
    public String getCommandType() {
        return commandType;
    }
}
