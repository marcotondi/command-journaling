package dev.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.domain.Command;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandFactoryImpl implements ICommandFactory {
    private static final Logger LOG = Logger.getLogger(CommandFactoryImpl.class);

    @Inject
    Instance<Command<?>> commandPrototypes;

    private Map<String, Class<? extends Command<?>>> commandClassMap;

    void onStart(@Observes StartupEvent ev) {
        commandClassMap = commandPrototypes.stream()
                .filter(cmd -> getBeanClass(cmd).isAnnotationPresent(CommandType.class))
                .collect(Collectors.toMap(
                        cmd -> getBeanClass(cmd).getAnnotation(CommandType.class).value(),
                        cmd -> (Class<? extends Command<?>>) getBeanClass(cmd),
                        (existing, replacement) -> {
                            LOG.warnf("Duplicate command type found for '%s'. Using %s and ignoring %s.",
                                    getBeanClass(replacement).getAnnotation(CommandType.class).value(),
                                    existing.getName(), replacement.getName());
                            return existing;
                        }));
        LOG.infof("Initialized Command Registry with %d providers: %s", commandClassMap.size(),
                commandClassMap.keySet());
    }

    @Override
    public <R> ICommand<R> buildCommand(CommandDescriptor descriptor) {
        Command<R> command = createCommandInstance(descriptor.commandType());
        command.setDescriptor(descriptor);
        return command;
    }

    @Override
    public <R> ICommand<R> buildCommand(
            CommandTypeName commandType,
            String commandId,
            int payloadVersion,
            String actor,
            String payload,
            LocalDateTime startTime,
            ObjectMapper objectMapper) {

        Command<R> command = createCommandInstance(commandType);

        command.descriptorFromJournal(
                commandType, commandId, payloadVersion,
                actor, payload, startTime, objectMapper);

        return command;
    }

    private <R> Command<R> createCommandInstance(CommandTypeName commandType) {
        String commandTypeName = commandType.name();
        Class<? extends Command<?>> commandClass = commandClassMap.get(commandTypeName);

        if (commandClass == null) {
            throw new IllegalStateException("No provider found for command type: " + commandTypeName);
        }

        Instance<?> provider = CDI.current().select(commandClass);
        return (Command<R>) provider.get();
    }

    private Class<?> getBeanClass(Object beanInstance) {
        Class<?> beanClass = beanInstance.getClass();
        if (beanClass.getName().contains("_ClientProxy")) {
            return beanClass.getSuperclass();
        }
        return beanClass;
    }

}
