package com.github.marcotondi.core.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.api.Commands;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.domain.Command;
import com.github.marcotondi.core.domain.CommandComposite;
import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.domain.CompositeDescriptor;
import com.github.marcotondi.core.domain.exception.CommandNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommandFactoryImpl implements ICommandFactory {
    private static final Logger LOG = Logger.getLogger(CommandFactoryImpl.class);

    private static final String COMMANDS_PAYLOAD_KEY = "commands";
    private static final String COMMAND_TYPE_PAYLOAD_KEY = "commandType";

    @Inject
    Instance<ICommand<?>> commandPrototypes;

    @Override
    public <R> ICommand<R> buildCommand(CommandDescriptor descriptor) {
        validateNotNull(descriptor, "CommandDescriptor cannot be null");
        return buildSingleCommand(descriptor);
    }

    @Override
    public <R> ICommand<R> buildCommand(String commandType, Map<String, Object> payload) {
        validateNotNull(commandType, "Command type cannot be null");
        validateNotNull(payload, "Payload cannot be null");

        var command = (ICommand) createCommandInstance(commandType);
        // String commandCategory = determineCommandCategory(payload);

        return command instanceof CommandComposite composite
                ? buildCompositeCommand(composite, payload)
                : buildSingleCommand(command, payload);
    }

    @Override
    public <R> ICommand<R> buildGroupCommand(CompositeDescriptor descriptor, CommandDescriptor[] descriptors) {
        validateNotNull(descriptor, "CompositeDescriptor cannot be null");
        validateNotNull(descriptors, "CommandDescriptor array cannot be null");

        var compositeCommand = (CommandComposite) createCommandInstance(descriptor.getCommandType());
        compositeCommand.setDescriptor(descriptor);

        Arrays.stream(descriptors)
                .map(this::buildSingleCommand)
                .forEach(compositeCommand::addCommand);

        return compositeCommand;
    }

    private <R> ICommand<R> buildCompositeCommand(ICommand icommand, Map<String, Object> payload) {
        var compositeCommand = (CommandComposite) icommand;

        Object commandsObj = payload.get(COMMANDS_PAYLOAD_KEY);

        if (commandsObj instanceof List commandDescriptors) {
            buildCommandsFromDescriptors(compositeCommand, commandDescriptors);
        } else {
            buildCommandsFromAnnotation(compositeCommand, payload);
        }

        compositeCommand.setDescriptor(payload);
        return compositeCommand;
    }

    private <R> void buildCommandsFromDescriptors(ICommand<R> icommand, List<Map<String, Object>> descriptors) {
        if (descriptors == null || descriptors.isEmpty()) {
            LOG.warnf("No command descriptors provided for composite command");
            return;
        }

        var compositeCommand = (CommandComposite) icommand;

        descriptors.forEach(descriptor -> {
            String subCommandType = (String) descriptor.get(COMMAND_TYPE_PAYLOAD_KEY);
            if (subCommandType == null) {
                LOG.warnf("Skipping descriptor without command type: %s", descriptor);
                return;
            }
            ICommand<R> subCommand = buildSingleCommand(subCommandType, descriptor);
            ((CommandComposite) compositeCommand).addCommand(subCommand);
        });
    }

    private <R> void buildCommandsFromAnnotation(ICommand<R> icommand, Map<String, Object> payload) {
        var compositeCommand = (CommandComposite) icommand;
        Commands commandsAnnotation = compositeCommand.getClass().getAnnotation(Commands.class);

        if (commandsAnnotation == null) {
            throw new IllegalStateException(
                    "No @Commands annotation found on composite command: " + compositeCommand.getClass().getName());
        }

        String[] commandTypes = commandsAnnotation.commandTypes();
        if (commandTypes == null || commandTypes.length == 0) {
            throw new IllegalStateException(
                    "Empty commandTypes in @Commands annotation on: " + compositeCommand.getClass().getName());
        }

        Arrays.stream(commandTypes)
                .map(cmdType -> this.<R>buildSingleCommand(cmdType, payload))
                .forEach(compositeCommand::addCommand);
    }

    private <R> ICommand<R> buildSingleCommand(CommandDescriptor descriptor) {
        Command<R> command = (Command<R>) createCommandInstance(descriptor.getCommandType());
        command.setDescriptor(descriptor);
        return command;
    }

    private <R> ICommand<R> buildSingleCommand(String type, Map<String, Object> payload) {
        Command<R> command = (Command<R>) createCommandInstance(type);
        command.setDescriptor(payload);
        return command;
    }

    private <R> ICommand<R> buildSingleCommand(ICommand<R> command, Map<String, Object> payload) {
        ((Command<R>) command).setDescriptor(payload);
        return command;
    }

    private <R> ICommand<R> createCommandInstance(String commandType) {
        validateNotNull(commandType, "Command type cannot be null");

        LOG.debugf("Searching for Command annotated with type: %s", commandType);

        return (ICommand<R>) commandPrototypes.stream()
                .filter(this::hasCommandTypeAnnotation)
                .filter(cmd -> getCommandTypeValue(cmd).equals(commandType))
                .findFirst()
                .orElseThrow(() -> new CommandNotFoundException(commandType));
    }

    private boolean hasCommandTypeAnnotation(ICommand<?> command) {
        return getBeanClass(command).isAnnotationPresent(CommandType.class);
    }

    private String getCommandTypeValue(ICommand<?> command) {
        return getBeanClass(command).getAnnotation(CommandType.class).value();
    }

    private Class<?> getBeanClass(Object beanInstance) {
        Class<?> beanClass = beanInstance.getClass();

        // Handle CDI proxies more robustly
        while (beanClass.getName().contains("$Proxy") ||
                beanClass.getName().contains("_ClientProxy") ||
                beanClass.getName().contains("$$")) {
            beanClass = beanClass.getSuperclass();
            if (beanClass == null || beanClass == Object.class) {
                return beanInstance.getClass();
            }
        }

        return beanClass;
    }

    private void validateNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
