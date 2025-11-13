package dev.marcotondi.core.domain.exception;

public class CommandExecutionException extends RuntimeException {
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
