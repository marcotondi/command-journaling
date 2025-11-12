package dev.marcotondi.infra;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CompositeCommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CompositeCommandManager implements Command<Void, CompositeCommandDescriptor<Void>> {

    @Inject
    CommandManager dispatcher;

    @Override
    public Void execute(CompositeCommandDescriptor<Void> command) {
        for (CommandDescriptor<?> cmd : command.commands()) {
            dispatcher.dispatch(cmd);
        }
        return null;
    }

    @Override
    public Void undo(CompositeCommandDescriptor<Void> command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'undo'");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<CompositeCommandDescriptor<Void>> getCommandType() {
        return (Class<CompositeCommandDescriptor<Void>>) (Class<?>) CompositeCommandDescriptor.class;
    }

}
