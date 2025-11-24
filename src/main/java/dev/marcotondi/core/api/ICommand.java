package dev.marcotondi.core.api;

import dev.marcotondi.core.domain.CommandDescriptor;

/**
 * Defines a handler for a specific type of command.
 * Handlers contain the business logic to be executed for a command.
 *
 * @param <R> The result type of the command execution.
 */
public interface ICommand<R> {

    CommandDescriptor getDescriptor();

    R execute();

    R undo();

}
