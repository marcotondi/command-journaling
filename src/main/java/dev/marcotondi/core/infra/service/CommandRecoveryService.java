package dev.marcotondi.core.infra.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandReconstructor;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.infra.CommandFactory;
import dev.marcotondi.core.infra.CommandManager;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.journal.infra.repository.JournalRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandRecoveryService {
    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    @Inject
    private JournalRepository journalRepository;
    @Inject
    private CommandManager dispatcher;
    @Inject
    private CommandFactory commandFactory;
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private Instance<CommandReconstructor> reconstructorInstances;

    private Map<CommandTypeName, CommandReconstructor> reconstructors;

    @PostConstruct
    public void init( ) {
        this.reconstructors = new HashMap<>();
        for (CommandReconstructor reconstructor : reconstructorInstances) {
            reconstructors.put(reconstructor.supportedType(), reconstructor);
        }
        LOG.infof("Loaded %d command reconstructors.", this.reconstructors.size());
    }

    void onStart(@Observes @Priority(Integer.MAX_VALUE) StartupEvent ev) {
        LOG.info("Starting recovery of interrupted commands...");
        recoverInterruptedCommands();
    }

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

                CommandDescriptor descriptor = reconstructCommand(entry);
                Command<?> command = commandFactory.buildCommand(descriptor);
                dispatcher.executeInTransaction(command, entry);

                LOG.infof("Successfully recovered command ID: %s", entry.commandId);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command %s. Manual intervention may be required.", entry.commandId);
            }
        }
    }

    private CommandDescriptor reconstructCommand(JournalEntry entry) throws Exception {
        CommandReconstructor reconstructor = reconstructors.get(entry.commandType);
        if (reconstructor == null) {
            // This can happen for CompositeCommands or other types that don't have a reconstructor.
            // This is not necessarily an error, as their recovery might follow a different path.
            throw new IllegalStateException("No reconstructor found for command type: " + entry.commandType);
        }
        // Delegate the actual reconstruction to the application-specific component
        return reconstructor.reconstruct(entry, objectMapper);
    }
}
