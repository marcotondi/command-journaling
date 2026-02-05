package com.github.marcotondi.core.api;

import java.util.Map;

import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.domain.CompositeDescriptor;

public interface ICommandFactory {

    <R> ICommand<R> buildCommand(CommandDescriptor descriptor);

    <R> ICommand<R> buildCommand(
            String commandType,
            Map<String, Object> payloadMap);

    <R> ICommand<R> buildGroupCommand(
            CompositeDescriptor compositeDescriptor,
            CommandDescriptor[] descriptors);
}
