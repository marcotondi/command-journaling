package dev.marcotondi.core.domain;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import dev.marcotondi.core.api.ICommand;

public abstract class CompositeDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 8253349985L;

    private final CommandDescriptor[] descriptors;

    private Queue<ICommand<?>> executeCommand;

    public CompositeDescriptor(
            String commandType,
            String actor,
            CommandDescriptor... descriptors) {

        super(commandType, actor);
        this.descriptors = descriptors;
        this.executeCommand = new ArrayDeque<>();
    }

    public CompositeDescriptor(
            UUID commandId,
            LocalDateTime timestamp,
            String commandType,
            String actor,
            CommandDescriptor... descriptors) {

        super(commandId, timestamp, commandType, actor);
        this.descriptors = descriptors;
        this.executeCommand = new ArrayDeque<>();
    }

    public CommandDescriptor[] getDescriptors() {
        return this.descriptors;
    }


    public Queue<ICommand<?>> getExecuteCommand() {
        return this.executeCommand;
    }

    protected void addExecuteCommand(ICommand<?> executeCommand) {
        this.executeCommand.add(executeCommand);
    }

}
