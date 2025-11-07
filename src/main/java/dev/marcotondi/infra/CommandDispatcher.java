package dev.marcotondi.infra;

import java.time.Duration;
import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.entry.JournalEntry;
import dev.marcotondi.domain.exception.CommandExecutionException;
import dev.marcotondi.domain.handler.CommandHandler;
import dev.marcotondi.domain.model.Command;
import dev.marcotondi.domain.model.CommandExecutedEvent;
import dev.marcotondi.domain.model.CommandStatus;
import dev.marcotondi.infra.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbException;

@ApplicationScoped
public class CommandDispatcher {

    private static final Logger LOG = Logger.getLogger(CommandDispatcher.class);

    @Inject
    CommandHandlerRegistry commandHandlerRegistry;

    @Inject
    JournalRepository journalRepository;

    @Inject
    Event<CommandExecutedEvent> commandEventPublisher;

    @Inject
    Jsonb jsonb;

    public <R> R dispatch(Command<R> command) {
        JournalEntry entry = createJournalEntry(command, CommandStatus.PENDING);
        journalRepository.persist(entry);

        return dispatch(command, entry);
    }

    public <R> R dispatch(Command<R> command, JournalEntry entry) {
        try {
            final CommandHandler<R, Command<R>> handler = commandHandlerRegistry.getHandlerFor(command);

            LOG.debugf("Dispatching command: %s", command.commandType());

            updateJournalStatus(entry, CommandStatus.EXECUTING);

            LocalDateTime startTime = LocalDateTime.now();
            R result = handler.handle(command);
            LocalDateTime endTime = LocalDateTime.now();

            long duration = Duration.between(startTime, endTime).toMillis();
            updateJournalOnSuccess(entry, result, duration);

            commandEventPublisher.fireAsync(new CommandExecutedEvent(command.commandId().toString(), command.commandType(), result));

            return result;
        } catch (Exception e) {
            LOG.errorf(e, "Command execution failed for command ID %s", command.commandId());
            updateJournalOnFailure(entry, e);
            throw new CommandExecutionException("Failed to execute command: " + command.commandId(), e);
        }
    }

    private JournalEntry createJournalEntry(Command<?> command, CommandStatus status) {
        JournalEntry entry = new JournalEntry();
        entry.setCommandId(command.commandId().toString());
        entry.setCommandType(command.commandType());
        entry.setActor(command.actor());
        entry.setStartTime(LocalDateTime.now());
        entry.setStatus(status.name());
        try {
            entry.setCommandPayload(jsonb.toJson(command));
        } catch (JsonbException e) {
            LOG.errorf(e, "Error serializing command payload for command ID %s", command.commandId());
            entry.setCommandPayload("{\"error\": \"Serialization failed\"}");
        }
        return entry;
    }

    private void updateJournalStatus(JournalEntry entry, CommandStatus status) {
        entry.setStatus(status.name());
        journalRepository.update(entry);
    }

    private <R> void updateJournalOnSuccess(JournalEntry entry, R result, long duration) {
        entry.setEndTime(LocalDateTime.now());
        entry.setStatus(CommandStatus.COMPLETED.name());
        entry.setExecutionTimeMs(duration);
        try {
            entry.setResult(jsonb.toJson(result));
        } catch (JsonbException e) {
            LOG.errorf(e, "Error serializing result for command ID %s", entry.getCommandId());
            entry.setResult("{\"error\": \"Result serialization failed\"}");
        }
        journalRepository.update(entry);
    }

    private void updateJournalOnFailure(JournalEntry entry, Exception e) {
        entry.setEndTime(LocalDateTime.now());
        entry.setStatus(CommandStatus.FAILED.name());
        entry.setErrorDetails(e.getClass().getName() + ": " + e.getMessage());
        journalRepository.update(entry);
    }
}
