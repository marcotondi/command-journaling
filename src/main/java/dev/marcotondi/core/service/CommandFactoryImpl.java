package dev.marcotondi.core.service;

import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandComposite;
import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.domain.CompositeDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
@SuppressWarnings({"rawtypes", "unchecked"})
public class CommandFactoryImpl implements ICommandFactory {
    private static final Logger LOG = Logger.getLogger(CommandFactoryImpl.class);

    @Inject
    Instance<ICommand<?>> commandPrototypes;

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

        ICommand<R> command;

        if (descriptor instanceof CompositeDescriptor compositeDescriptor)
            command = buildCompositeCommand(compositeDescriptor);
        else
            command = buildSingleCommand(descriptor);

        return command;
    }

    @Override
    public <R> ICommand<R> buildCommand(
            CommandTypeName commandType,
            Map<String, Object> payload) {

        String typeCommand = payload.containsKey("descriptors") ? "COMPOSITE" : "SIMPLE";

        ICommand<R> command;

        if (typeCommand.equalsIgnoreCase("COMPOSITE"))
            command = buildCompositeCommand(commandType, payload);
        else
            command = buildSingleCommand(commandType, payload);

        // Command<R> command = (Command<R>) createCommandInstance(commandType);
        // command.setDescriptor(payload);

        return command;
    }

    private <R> ICommand<R> buildCompositeCommand(CompositeDescriptor descriptor) {

        var composite = (CommandComposite) createCommandInstance(descriptor.getCommandType());
        composite.setDescriptor(descriptor);

        for (int i = 0; i < descriptor.getDescriptors().length; i++) {
            var _d = descriptor.getDescriptors()[i];

            ICommand<R> command = buildSingleCommand(_d);
            composite.addCommand(command);
        }

        return composite;
    }

    private <R> ICommand<R> buildCompositeCommand(
            CommandTypeName commandType,
            Map<String, Object> payload) {

    var composite = (CommandComposite) createCommandInstance(commandType);

    // Ottieni la lista dei descrittori
    List<Map<String, Object>> descriptors = (List<Map<String, Object>>) payload.get("descriptors");
    CommandDescriptor[] descriptorArray = new CommandDescriptor[descriptors != null ? descriptors.size() : 0];

    if (descriptors != null) {
        for (Map<String, Object> descriptor : descriptors) {
            CommandTypeName subCommandType =  CommandTypeName.valueOf((String) descriptor.get("commandType"));
            ICommand<R> command = buildSingleCommand(subCommandType, descriptor);
            composite.addCommand(command);

            descriptorArray[descriptors.indexOf(descriptor)] = command.getDescriptor();
        }

        composite.setDescriptor(payload, descriptorArray);
    }

    return composite;
    }

    private <R> ICommand<R> buildSingleCommand(CommandDescriptor descriptor) {
        Command<R> command = (Command<R>) createCommandInstance(descriptor.getCommandType());
        command.setDescriptor(descriptor);

        return command;
    }

    private <R> ICommand<R> buildSingleCommand(
            CommandTypeName commandType,
            Map<String, Object> payload) {

        Command<R> command = (Command<R>) createCommandInstance(commandType);
        command.setDescriptor(payload);

        return command;
    }

    private <R> ICommand<R> createCommandInstance(CommandTypeName commandType) {
        LOG.infof("Searching for Command annotated with type %s", commandType.name());

        return commandPrototypes.stream()
                .filter(cmd -> getBeanClass(cmd).isAnnotationPresent(CommandType.class))
                .filter(cmd -> getBeanClass(cmd).getAnnotation(CommandType.class).value()
                        .equals(commandType.name()))
                .findFirst()
                .map(cmd -> (ICommand<R>) cmd)
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
