package com.github.marcotondi.core.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.domain.exception.CommandExecutionException;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;

public abstract class CommandComposite<R> implements ICommand<List<R>> {
    private static final Logger LOG = Logger.getLogger(CommandComposite.class);

    @Inject
    JournalService journal;

    private List<Command<R>> commands = new ArrayList<>();
    private CompositeDescriptor descriptor;

    public abstract CommandDescriptor setDescriptor(Map<String, Object> payload);

    @Override
    public List<R> execute() throws CommandExecutionException {
        LOG.debugf("Execute CommandComposite");

        var commandDescriptors = commandDescriptors();

        JournalEntity entry = journal.getOrCreateEntry(
                getDescriptor(),
                commandDescriptors);

        List<R> results = new ArrayList<>();
        journal.updateJournalStatus(entry, CommandStatus.EXECUTING);
        long startNano = System.nanoTime();

        for (Command<R> command : commands) {
            LOG.debugf("Executing command: %s", command.getDescriptor().getCommandType());

            try {

                var result = command.doExecute();
                descriptor.addExecuteCommandId(command.getDescriptor().getCommandId());
                results.add(result);

                journal.updateJournalPayload(entry, descriptor);

            } catch (Exception e) {
                LOG.errorf(e, "Command execution failed for command ID %s",
                        command.getDescriptor().getCommandId());
                journal.updateJournalOnFailure(entry, e);

                throw new CommandExecutionException(
                        "Execution failed for sub-command " +
                                command.getClass().getSimpleName(),
                        e);
            }
        }
        long duration = Duration.ofNanos(System.nanoTime() - startNano).toMillis();
        journal.updateJournalOnSuccess(entry, results, duration);

        LOG.infof("Done command: %s", entry.commandId);

        return results;
    }

    @Override
    public List<R> undo() {
        List<R> results = new ArrayList<>();
        List<Exception> failures = new ArrayList<>();

        var commandDescriptors = commandDescriptors();

        JournalEntity entry = journal.getOrCreateEntry(
                getDescriptor(),
                commandDescriptors);

        Iterator<String> it = descriptor.getExecuteCommandId().descendingIterator();
        long startNano = System.nanoTime();

        while (it.hasNext()) {
            String commandId = it.next();
            LOG.debugf("Rollback command in: %s", commandId);

            Command<R> targetCommand = commands.stream()
                    .filter(cmd -> commandId.equals(cmd.getDescriptor().getCommandId()))
                    .findFirst()
                    .orElseThrow(() -> new CommandExecutionException(
                            "Command not found for ID: " + commandId));

            try {
                results.add(targetCommand.doUndo());
            } catch (Exception e) {
                LOG.errorf(e, "Undo-Command execution failed for command ID %s", commandId);
                failures.add(e);
            }
        }

        long durationMs = Duration.ofNanos(System.nanoTime() - startNano).toMillis();
        journal.updateJournalOnRollBack(entry, results, durationMs);

        if (!failures.isEmpty()) {
            String details = failures.stream()
                    .map(Exception::getMessage)
                    .collect(Collectors.joining(", "));

            throw new CommandExecutionException(
                    "Partial rollback failure [" + failures.size() + " errors]: " + details,
                    failures.get(0));
        }

        return results;
    }

    @Override
    public CommandDescriptor getDescriptor() {
        return this.descriptor;
    }

    public void setDescriptor(CommandDescriptor descriptor) {
        if (!(descriptor instanceof CompositeDescriptor)) {
            throw new IllegalArgumentException("CommandComposite requires a CompositeDescriptor");
        }
        this.descriptor = (CompositeDescriptor) descriptor;
    }

    public List<Command<R>> getCommands() {
        return this.commands;
    }

    public void addCommand(ICommand<R> command) {
        this.commands.add((Command<R>) command);
    }

    private CommandDescriptor[] commandDescriptors() {
        return commands.stream()
                .map(Command::getDescriptor)
                .toArray(CommandDescriptor[]::new);
    }
}
