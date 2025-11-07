package dev.marcotondi.domain.handler;

import dev.marcotondi.domain.model.Command;
import dev.marcotondi.domain.model.CompositeCommand;
import dev.marcotondi.infra.CommandDispatcher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CompositeCommandHandler implements CommandHandler<Void, CompositeCommand> {

    @Inject
    CommandDispatcher dispatcher;

    @Override
    public Void handle(CompositeCommand command) {
        for (Command<?> cmd : command.commands()) {
            dispatcher.dispatch(cmd);
        }
        return null;
    }

    @Override
    public Class<CompositeCommand> getCommandType() {
        return CompositeCommand.class;
    }
}
