package dev.marcotondi.core.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.api.ICommandManager;
import dev.marcotondi.core.entity.JournalEntity;
import dev.marcotondi.core.repository.JournalRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
class CommandRecoveryService {
    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    @Inject
    ICommandManager dispatcher;
    @Inject
    ICommandFactory commandFactory;

    @Inject
    JournalRepository journalRepository;

    @Inject
    ObjectMapper objectMapper;

    void onStart(@Observes @Priority(Integer.MAX_VALUE) StartupEvent ev) {
        LOG.info("Starting recovery of interrupted commands...");
        recoverInterruptedCommands();
    }

    private void recoverInterruptedCommands() {
        List<JournalEntity> interrupted = journalRepository.findInterruptedCommands();
        if (interrupted.isEmpty()) {
            LOG.info("No interrupted commands found. Recovery not needed.");
            return;
        }

        LOG.infof("Found %d interrupted commands to recover.", interrupted.size());

        for (JournalEntity entry : interrupted) {
            try {
                LOG.infof("Attempting to recover command ID: %s (%s)", entry.commandId, entry.commandType);

                ICommand<?> command = commandFactory
                        .buildCommand(
                                entry.commandType,
                                entry.payload,
                                entry.startTime
                                );

                dispatcher.dispatch(command);

                LOG.infof("Successfully recovered command ID: %s", entry.commandId);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command %s. Manual intervention may be required.", entry.commandId);
            }
        }
    }

}
