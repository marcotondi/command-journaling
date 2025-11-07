package dev.marcotondi.domain.handler;

import dev.marcotondi.domain.model.Command;

/**
 * Defines a handler for a specific type of command.
 * Handlers contain the business logic to be executed for a command.
 *
 * @param <R> The result type of the command execution.
 * @param <T> The command type this handler can process.
 */
public interface CommandHandler<R, T extends Command<R>> {

    /**
     * Handles the command execution.
     *
     * @param command The command to handle.
     * @return The result of the execution.
     */
    R handle(T command);

    /**
     * Returns the class of the command this handler is responsible for.
     * Used for mapping commands to handlers in the dispatcher.
     *
     * @return The command's class.
     */
    Class<T> getCommandType();
}
