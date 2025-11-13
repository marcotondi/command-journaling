package dev.marcotondi.domain.api;

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

    String commandType();
}
