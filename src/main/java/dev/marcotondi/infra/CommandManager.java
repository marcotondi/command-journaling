package dev.marcotondi.infra;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.CommandStatus;
import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.domain.exception.CommandExecutionException;
import dev.marcotondi.domain.model.CommandExecutedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;


@ApplicationScoped
public class CommandManager {

    private static final Logger LOG = Logger.getLogger(CommandManager.class);

    @Inject
    CommandRegistry commandRegistry;

    @Inject
    JournalService journalService;

    @Inject
    Event<CommandExecutedEvent> commandEventPublisher;


    public <R> CompletableFuture<R> dispatchAsync(CommandDescriptor<R> command) {
        return CompletableFuture.supplyAsync(() -> dispatch(command));
    }

    public <R> R dispatch(CommandDescriptor<R> command) {
        JournalEntry entry = journalService.createJournalEntry(command, CommandStatus.PENDING);
        return dispatch(command, entry);
    }

    public <R> R dispatch(CommandDescriptor<R> command, JournalEntry entry) {
        try {
            final Command<R, CommandDescriptor<R>> registry = commandRegistry.getComandRegistryFor(command);

            LOG.debugf("Dispatching command: %s", command.commandType());

            journalService.updateJournalStatus(entry, CommandStatus.EXECUTING);

            LocalDateTime startTime = LocalDateTime.now();
            R result = registry.execute(command);
            LocalDateTime endTime = LocalDateTime.now();

            long duration = Duration.between(startTime, endTime).toMillis();
            journalService.updateJournalOnSuccess(entry, result, duration);

            commandEventPublisher.fireAsync(new CommandExecutedEvent(command.commandId().toString(), command.commandType(), result));

            return result;
        } catch (Exception e) {
            LOG.errorf(e, "Command execution failed for command ID %s", command.commandId());
            journalService.updateJournalOnFailure(entry, e);
            throw new CommandExecutionException("Failed to execute command: " + command.commandId(), e);
        }
    }

}
