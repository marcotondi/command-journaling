package com.github.marcotondi.core.domain;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public abstract class CompositeDescriptor extends CommandDescriptor {
    private static final long serialVersionUID = 8253349985L;

    private Queue<String> executeCommandId;

    public CompositeDescriptor(
            String commandType,
            String actor) {

        super(commandType, actor);
        this.executeCommandId = new ArrayDeque<>();
    }

    public CompositeDescriptor(
            UUID commandId,
            LocalDateTime timestamp,
            String commandType,
            String actor) {

        super(commandId, timestamp, commandType, actor);
        this.executeCommandId = new ArrayDeque<>();
    }

    public Queue<String> getExecuteCommandId() {
        return this.executeCommandId;
    }

    protected void addExecuteCommandId(String executeCommandId) {
        this.executeCommandId.add(executeCommandId);
    }

}
