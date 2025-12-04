package dev.marcotondi.core.api;

import java.util.Map;

import dev.marcotondi.core.domain.CommandDescriptor;

public interface ICommandFactory {

    <R> ICommand<R> buildCommand(CommandDescriptor descriptor);

    <R> ICommand<R> buildCommand(
            String commandType,
            Map<String, Object> payloadMap);
}
