package dev.marcotondi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all commands.
 * A command is an immutable data structure that represents a request to perform an action.
 *
 * @param <R> The type of the result produced by the command's execution.
 */
public interface Command<R> {

    UUID commandId();

    LocalDateTime timestamp();

    String actor();

    String commandType();
}