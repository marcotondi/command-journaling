package dev.marcotondi.service;

import java.util.List;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.entry.JournalEntry;
import dev.marcotondi.domain.model.Command;
import dev.marcotondi.infra.CommandDispatcher;
import dev.marcotondi.infra.repository.JournalRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

@ApplicationScoped
public class CommandRecoveryService {

    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    @Inject
    JournalRepository journalRepository;

    @Inject
    CommandDispatcher dispatcher;

    @Inject
    Jsonb jsonb;

    void onStart(@Observes @Priority(Integer.MAX_VALUE) StartupEvent ev) {
        LOG.info("Starting recovery of interrupted commands...");
        recoverInterruptedCommands();
    }

    /**
     * Recovers commands that were interrupted (e.g., due to an application crash).
     * It finds commands in PENDING or EXECUTING state and attempts to re-process them.
     */
    public void recoverInterruptedCommands() {
        List<JournalEntry> interrupted = journalRepository.findInterruptedCommands();
        if (interrupted.isEmpty()) {
            LOG.info("No interrupted commands found. Recovery not needed.");
            return;
        }

        LOG.infof("Found %d interrupted commands to recover.", interrupted.size());

        for (JournalEntry entry : interrupted) {
            try {
                LOG.infof("Attempting to recover command ID: %s (%s)", entry.getCommandId(), entry.getCommandType());
                Command<?> command = reconstructCommand(entry);
                dispatcher.dispatch(command, entry);
                LOG.infof("Successfully recovered command ID: %s", entry.getCommandId());
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command %s. Manual intervention may be required.", entry.getCommandId());
                // Optionally, update the journal entry to a specific RECOVERY_FAILED status
            }
        }
    }

    private Command<?> reconstructCommand(JournalEntry entry) throws Exception {
        // The commandType in the journal now holds the fully qualified class name,
        // making reconstruction reliable.
        Class<?> commandClass = Class.forName(entry.getCommandType());
        return (Command<?>) jsonb.fromJson(entry.getCommandPayload(), commandClass);
    }
}
