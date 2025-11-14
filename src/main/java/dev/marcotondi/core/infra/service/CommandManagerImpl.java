package dev.marcotondi.core.infra.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import dev.marcotondi.composite.model.CompositeCommandDescriptor;
import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandStatus;
import dev.marcotondi.core.domain.exception.CommandExecutionException;
import dev.marcotondi.core.domain.model.CommandExecutedEvent;
import dev.marcotondi.core.infra.CommandManager;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.journal.infra.api.JournalService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandManagerImpl implements CommandManager {
    private static final Logger LOG = Logger.getLogger(CommandManagerImpl.class);

    @Inject
    JournalService journalService;

    @Inject
    @Any
    Event<CommandExecutedEvent> commandEventPublisher;

    @Override
    public <R> CompletableFuture<R> dispatchAsync(Command<R> command) {
        // First, create the entry so it's visible immediately in its own transaction.
        JournalEntry entry = createAndPersistInitialEntry(command);
        // Then, run the long execution part asynchronously in a separate transaction.
        return CompletableFuture.supplyAsync(() -> executeInTransaction(command, entry));
    }

    @Override
    public <R> R dispatch(Command<R> command) {
        JournalEntry entry = createAndPersistInitialEntry(command);
        return executeInTransaction(command, entry);
    }

    // @Override
    public <R> R dispatch(Command<R> command, JournalEntry entry) {
        return executeInTransaction(command, entry);
    }

    // @Transactional(Transactional.TxType.REQUIRES_NEW)
    public JournalEntry createAndPersistInitialEntry(Command<?> command) {
        LOG.debugf("Creating initial journal entry for command: %s", command.getDescriptor().commandType());
        JournalEntry entry = journalService.createJournalEntry(
                command.getDescriptor(),
                CommandStatus.PENDING);

        if (command.getDescriptor() instanceof CompositeCommandDescriptor composite) {
            createChildJournalEntries(composite, entry);
        }
        return entry;
    }

    // @Transactional(Transactional.TxType.REQUIRES_NEW)
    public <R> R executeInTransaction(Command<R> command, JournalEntry entry) {
        // The entry was created in another transaction. We need to get a managed instance.
        JournalEntry managedEntry = journalService.getEntriesByCommandId(entry.commandId);

        final CommandDescriptor descriptor = command.getDescriptor();
        try {
            LOG.debugf("Executing command in transaction: %s", descriptor.commandType());

            journalService.updateJournalStatus(managedEntry, CommandStatus.EXECUTING);

            LocalDateTime startTime = LocalDateTime.now();
            R result = command.execute(); // This is the long-running part
            LocalDateTime endTime = LocalDateTime.now();
            long duration = Duration.between(startTime, endTime).toMillis();

            journalService.updateJournalOnSuccess(managedEntry, result, duration);

            // Publish event after success
            commandEventPublisher.fireAsync(
                    new CommandExecutedEvent(
                            descriptor.commandId().toString(),
                            descriptor.commandType().name(),
                            result));

            return result;

        } catch (Exception e) {
            LOG.errorf(e, "Command execution failed for command ID %s", descriptor.commandId());
            journalService.updateJournalOnFailure(managedEntry, e);
            throw new CommandExecutionException(
                    "Failed to execute command: " + descriptor.commandId(), e);
        }
    }

    private void createChildJournalEntries(
            CompositeCommandDescriptor composite,
            JournalEntry parentEntry) {

        for (CommandDescriptor childDescriptor : composite.childCommands()) {
            JournalEntry childEntry = journalService.createJournalEntry(
                    childDescriptor,
                    CommandStatus.PENDING);

            // Collega il child al parent
            journalService.linkChildToParent(childEntry, parentEntry);

            // Ricorsione per comandi compositi annidati
            if (childDescriptor instanceof CompositeCommandDescriptor childComposite) {
                createChildJournalEntries(childComposite, childEntry);
            }
        }
    }
}
