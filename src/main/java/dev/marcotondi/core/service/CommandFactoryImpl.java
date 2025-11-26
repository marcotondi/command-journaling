package dev.marcotondi.core.service;

import java.util.Map;

import org.jboss.logging.Logger;

import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandFactoryImpl implements ICommandFactory {
    private static final Logger LOG = Logger.getLogger(CommandFactoryImpl.class);

    @Inject
    Instance<Command<?>> commandPrototypes;

    // private Map<CommandTypeName, Command<?>> commandInstanceMap;
    // private Map<String, Class<? extends Command<?>>> commandClassMap;
    // void onStart(@Observes StartupEvent ev) {
    // commandInstanceMap = commandPrototypes.stream()
    // .filter(cmd -> getBeanClass(cmd).isAnnotationPresent(CommandType.class))
    // .collect(Collectors.toMap(
    // cmd -> getBeanClass(cmd).getAnnotation(CommandType.class).value(),
    // cmd -> cmd
    // ));
    // LOG.infof("Initialized Command Registry with %d providers: %s",
    // commandClassMap.size(),
    // commandClassMap.keySet());
    // }

    @Override
    public <R> ICommand<R> buildCommand(CommandDescriptor descriptor) {
        Command<R> command = createCommandInstance(descriptor.getCommandType());
        command.setDescriptor(descriptor);
        return command;
    }

    @Override
    public <R> ICommand<R> buildCommand(
            CommandTypeName commandType,
            Map<String, Object> payload) {

        Command<R> command = createCommandInstance(commandType);
        command.setDescriptor(payload);

        return command;
    }

    private <R> Command<R> createCommandInstance(CommandTypeName commandType) {
        LOG.infof("Seach Command with %s Annotatio Type", commandType.name());

        return commandPrototypes.stream()
                .filter(cmd -> getBeanClass(cmd).isAnnotationPresent(CommandType.class))
                .filter(cmd -> getBeanClass(cmd).getAnnotation(CommandType.class).value().equals(commandType.name()))
                .findFirst()
                .map(cmd -> (Command<R>) cmd)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No command found for type: " + commandType));
    }

    private Class<?> getBeanClass(Object beanInstance) {
        Class<?> beanClass = beanInstance.getClass();
        if (beanClass.getName().contains("_ClientProxy")) {
            return beanClass.getSuperclass();
        }
        return beanClass;
    }

    // private <R> Command<R> createCommandInstance(CommandTypeName commandType) {
    // Command<R> command = (Command<R>) commandInstanceMap.get(commandType);
    // if (command == null) {
    // throw new IllegalStateException("No provider found for command type: " +
    // commandType.name());
    // }
    // return command;
    // }

}
