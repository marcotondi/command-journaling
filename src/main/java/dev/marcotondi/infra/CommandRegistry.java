package dev.marcotondi.infra;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandRegistry {

    private static final Logger LOG = Logger.getLogger(CommandRegistry.class);

    @Inject
    Instance<Command<?, ?>> commands;

    private Map<Class<? extends CommandDescriptor<?>>, Command<?, ?>> commandMap;

    void onStart(@Observes StartupEvent ev) {
        commandMap = commands.stream()
            .collect(Collectors.toMap(Command::getCommandType, Function.identity()));
        LOG.infof("Initialized Command Handler Registry with %d handlers.", commandMap.size());
    }

    @SuppressWarnings("unchecked")
    public <R> Command<R, CommandDescriptor<R>> getComandRegistryFor(CommandDescriptor<R> command) {
        final Command<R, CommandDescriptor<R>> commands = (Command<R, CommandDescriptor<R>>) commandMap.get(command.getClass());
        if (commands == null) {
            throw new IllegalStateException("No handler found for command: " + command.getClass().getName());
        }
        return commands;
    }
}
