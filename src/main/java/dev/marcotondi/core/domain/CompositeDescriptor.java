package dev.marcotondi.core.domain;

import dev.marcotondi.core.api.CommandTypeName;

public abstract class CompositeDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 8253349985L;

    private final CommandDescriptor[] descriptors;

    public CompositeDescriptor(CommandTypeName commandType, String actor, CommandDescriptor... descriptors) {
        super(commandType, actor);
        this.descriptors = descriptors;
    }

    public CommandDescriptor[] getDescriptors() {
        return this.descriptors;
    }

}
