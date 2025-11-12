package dev.marcotondi.infra.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.infra.CommandManager;
import dev.marcotondi.infra.repository.JournalRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandRecoveryService {

    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    @Inject
    JournalRepository journalRepository;

    @Inject
    CommandManager dispatcher;

    @Inject
    ObjectMapper objectMapper;

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
                CommandDescriptor<?> command = reconstructCommand(entry);
                dispatcher.dispatch(command, entry);
                LOG.infof("Successfully recovered command ID: %s", entry.getCommandId());
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command %s. Manual intervention may be required.", entry.getCommandId());
                // Optionally, update the journal entry to a specific RECOVERY_FAILED status
            }
        }
    }

    private CommandDescriptor<?> reconstructCommand(JournalEntry entry) throws Exception {
        String type = entry.getCommandType();
        String payload = entry.getCommandPayload();

        if (payload == null || payload.isBlank()) {
            throw new IllegalStateException("Command payload is missing from journal entry: " + entry.getCommandId());
        }

        // The commandType in the journal now holds the fully qualified class name,
        // making reconstruction reliable.
        Class<?> commandClass = Class.forName(type);
        Object commandDescriptor = objectMapper.readValue(payload, commandClass);
        
        return (CommandDescriptor<?>) commandDescriptor;
    }
}
