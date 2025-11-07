package dev.marcotondi.infra;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.handler.CommandHandler;
import dev.marcotondi.domain.model.Command;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandHandlerRegistry {

    private static final Logger LOG = Logger.getLogger(CommandHandlerRegistry.class);

    @Inject
    Instance<CommandHandler<?, ?>> commandHandlers;

    private Map<Class<? extends Command<?>>, CommandHandler<?, ?>> handlerMap;

    void onStart(@Observes StartupEvent ev) {
        handlerMap = commandHandlers.stream()
            .collect(Collectors.toMap(CommandHandler::getCommandType, Function.identity()));
        LOG.infof("Initialized Command Handler Registry with %d handlers.", handlerMap.size());
    }

    @SuppressWarnings("unchecked")
    public <R> CommandHandler<R, Command<R>> getHandlerFor(Command<R> command) {
        final CommandHandler<R, Command<R>> handler = (CommandHandler<R, Command<R>>) handlerMap.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler found for command: " + command.getClass().getName());
        }
        return handler;
    }
}
