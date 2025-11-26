package dev.marcotondi.core;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.CommandDescriptor;

public class SimpleCommandDescriptor extends CommandDescriptor{
    private static final long serialVersionUID = 8253349985L;

    public SimpleCommandDescriptor(String actor) {
        super(CommandTypeName.SIMPLE_COMMAND, actor);
    }

}
