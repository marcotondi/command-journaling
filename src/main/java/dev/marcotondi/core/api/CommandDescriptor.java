package dev.marcotondi.core.api;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all commands.
 * A command is an immutable data structure that represents a request to perform an action.
 */
public interface CommandDescriptor {

    UUID commandId();

    LocalDateTime timestamp();

    String actor();

    /**
     * Returns the stable, logical type of the command.
     * @return The {@link CommandTypeName} enum.
     */
    CommandTypeName commandType();

    /**
     * Returns the data payload of the command.
     * @return The {@link Payload} DTO.
     */
    Payload getPayload();
}
