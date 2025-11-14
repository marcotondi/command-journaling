package dev.marcotondi.core.api;

/**
 * Defines a handler for a specific type of command.
 * Handlers contain the business logic to be executed for a command.
 *
 * @param <R> The result type of the command execution.
 */
public interface Command<R> {

    /**
    *
    * @return
    */
    CommandDescriptor getDescriptor();

    /**
     * Execute the command run.
     *
     * @return The result of the execution.
     */
    R execute();

    /**
     * Undo the command execution.
     *
     * @return The result of the execution.
     */
    R undo();
}
