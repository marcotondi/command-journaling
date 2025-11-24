package dev.marcotondi.core.domain;

import dev.marcotondi.core.api.ICommand;

public abstract class CommandComposite<R> implements ICommand<R> {

    public CommandComposite(CommandDescriptor descriptor) {
        throw new UnsupportedOperationException("Composite commands cannot be executed directly.");
    }

}
