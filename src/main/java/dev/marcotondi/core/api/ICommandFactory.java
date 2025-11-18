package dev.marcotondi.core.api;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ICommandFactory {

    <R> ICommand<R> buildCommand(CommandDescriptor descriptor);

    <R> ICommand<R> buildCommand(
        CommandTypeName commandType,
        String commandId,
        int payloadVersion,
        String actor,
        String payload,
        LocalDateTime startTime,
        ObjectMapper objectMapper
    );
}
