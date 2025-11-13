package dev.marcotondi.infra;

import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CommandType;
import dev.marcotondi.domain.api.Initializable;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandFactory {

    private static final Logger LOG = Logger.getLogger(CommandFactory.class);

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

    public <R> Command<R> buildCommand(CommandDescriptor descriptor) {
        String commandType = descriptor.commandType();
        Class<? extends Command<?>> commandClass = commandClassMap.get(commandType);
        if (commandClass == null) {
            throw new IllegalStateException("No provider found for command type: " + commandType);
        }

        Instance<?> provider = CDI.current().select(commandClass);
        Command<R> command = (Command<R>) provider.get();

        if (command instanceof Initializable c) {
            c.init(descriptor);
        }

        return command;
    }

    private Class<?> getBeanClass(Object beanInstance) {
        Class<?> beanClass = beanInstance.getClass();
        if (beanClass.getName().contains("_ClientProxy")) {
            return beanClass.getSuperclass();
        }
        return beanClass;
    }
}
