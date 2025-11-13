package dev.marcotondi.infra.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.infra.CommandFactory;
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
    CommandFactory commandFactory;

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
                LOG.infof("Attempting to recover command ID: %s (%s)", entry.commandId, entry.commandType);

                // Step 1: Reconstruct the descriptor from the journal
                CommandDescriptor descriptor = reconstructCommand(entry);

                // Step 2: Create the executable Command object from the descriptor
                Command<?> command = commandFactory.buildCommand(descriptor);

                // Step 3: Dispatch the executable command for re-processing
                dispatcher.dispatch(command, entry);

                LOG.infof("Successfully recovered command ID: %s", entry.commandId);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command %s. Manual intervention may be required.", entry.commandId);
                // Optionally, update the journal entry to a specific RECOVERY_FAILED status
            }
        }
    }

    private CommandDescriptor reconstructCommand(JournalEntry entry) throws Exception {
        // This method assumes the 'commandType' field in the journal stores the
        // fully qualified class name of the CommandDescriptor.
        String descriptorClassName = entry.commandId;
        String payload = entry.commandPayload;

        if (payload == null || payload.isBlank()) {
            throw new IllegalStateException("Command payload is missing from journal entry: " + entry.commandId);
        }

        if (descriptorClassName == null || descriptorClassName.isBlank()) {
            throw new IllegalStateException("Command descriptor class name is missing from journal entry: " + entry.commandId);
        }

        Class<?> commandClass = Class.forName(descriptorClassName);
        Object commandDescriptor = objectMapper.readValue(payload, commandClass);

        return (CommandDescriptor) commandDescriptor;
    }
}
