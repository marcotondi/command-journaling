package dev.marcotondi.core.api;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Defines a handler for a specific type of command.
 * Handlers contain the business logic to be executed for a command.
 *
 * @param <R> The result type of the command execution.
 */
public interface ICommand<R> {

    void descriptorFromJournal(
            CommandTypeName type,
            String commandId,
            int payloadVersion,
            String actor,
            String payload,
            LocalDateTime startTime,
            ObjectMapper mapper
    );

    CommandDescriptor getDescriptor();

    R doExecute();

    R doUndo();

    R execute();

    R undo();



}
