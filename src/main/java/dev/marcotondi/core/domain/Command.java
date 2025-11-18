package dev.marcotondi.core.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.JournalService;
import dev.marcotondi.core.domain.exception.CommandExecutionException;
import dev.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;

public abstract class Command<R> implements ICommand<R> {
    private static final Logger LOG = Logger.getLogger(Command.class);

    @Inject
    JournalService journal;

    private CommandDescriptor descriptor;

    public void setDescriptor(CommandDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public CommandDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public abstract R doExecute();

    @Override
    public abstract R doUndo();

    @Override
    public final R execute() {
        LOG.debugf("Creating initial journal entry for command: %s", descriptor.commandType());

        JournalEntity entry = journal.getOrCreateEntry(
                getDescriptor(),
                CommandStatus.PENDING);

        try {
            LOG.debugf("Executing command: %s", descriptor.commandType());

            journal.updateJournalStatus(entry, CommandStatus.EXECUTING);

            LocalDateTime startTime = LocalDateTime.now();
            R result = doExecute(); // This is the long-running part
            long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();

            journal.updateJournalOnSuccess(entry, result, duration);

            return result;

        } catch (Exception e) {
            LOG.errorf(e, "Command execution failed for command ID %s", getDescriptor().commandId());
            journal.updateJournalOnFailure(entry, e);

            throw new CommandExecutionException(
                    "Failed to execute command: " + getDescriptor().commandId(), e);
        }
    }

    @Override
    public final R undo() {

        String commandId = getDescriptor().commandId().toString();

        JournalEntity entry = journal.findByCommandId(commandId)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot rollback command without journal entry: " + commandId));

        try {

            LOG.debugf("Rollback command in: %s", descriptor.commandType());

            journal.updateJournalStatus(entry, CommandStatus.EXECUTING_ROLL_BACK);

            LocalDateTime startTime = LocalDateTime.now();
            R result = doUndo();
            long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();

            journal.updateJournalOnRollBack(entry, result, duration);

            return result;

        } catch (Exception e) {
            LOG.errorf(e, "Undo-Command execution failed for command ID %s", commandId);
            journal.updateJournalOnFailure(entry, e);

            throw new CommandExecutionException(
                    "Undo failed for command: " + commandId, e);
        }

    }

}
