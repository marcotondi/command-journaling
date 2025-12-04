package dev.marcotondi.core.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;
import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.JournalService;
import dev.marcotondi.core.domain.exception.CommandExecutionException;
import dev.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;

public abstract class CommandComposite<R> implements ICommand<List<R>> {
    private static final Logger LOG = Logger.getLogger(CommandComposite.class);

    @Inject
    JournalService journal;

    private List<Command<R>> commands = new ArrayList<>();
    private CompositeDescriptor descriptor;

    public abstract CommandDescriptor setDescriptor(Map<String, Object> payload, CommandDescriptor... descriptors);

    @Override
    public List<R> execute() throws CommandExecutionException {
        LOG.debugf("Execute CommandComposite");

        JournalEntity entry = journal.getOrCreateEntry(
                getDescriptor(),
                CommandStatus.PENDING);

        List<R> results = new ArrayList<>();
        journal.updateJournalStatus(entry, CommandStatus.EXECUTING);
        LocalDateTime startTime = LocalDateTime.now();

        for (Command<R> command : commands) {
            LOG.debugf("Executing command: %s", command.getDescriptor().getCommandType());

            try {

                var result = command.doExecute();
                descriptor.addExecuteCommand(command);
                results.add(result);

                journal.updateJournalPayload(entry, descriptor);

            } catch (Exception e) {
                LOG.errorf(e, "Command execution failed for command ID %s",
                        command.getDescriptor().getCommandId());
                journal.updateJournalOnFailure(entry, e);

                throw new CommandExecutionException(
                        "Execution failed for sub-command " + command.getClass().getSimpleName(),
                        e);
            }
        }
        long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
        journal.updateJournalOnSuccess(entry, results, duration);

        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<R> undo() {
        List<R> results = new ArrayList<>();
        for (ICommand<?> command : descriptor.getExecuteCommand()) {
            LOG.debugf("Rollback command in: %s", command.getDescriptor().getCommandType());

            try {
                results.add((R) command.undo());
            } catch (Exception e) {
                LOG.errorf(e, "Undo-Command execution failed for command ID %s",
                        command.getDescriptor().getCommandId());

                throw new CommandExecutionException(
                        "Execution failed for sub-command " + command.getClass().getSimpleName(),
                        e);
            }
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
}
