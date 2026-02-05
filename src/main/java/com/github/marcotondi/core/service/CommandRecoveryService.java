package com.github.marcotondi.core.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.entity.JournalEntity;
import com.github.marcotondi.core.repository.JournalRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
class CommandRecoveryService {
    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);
    private static final String COMMANDS_PAYLOAD_KEY = "commands";

    @Inject
    ICommandManager manager;
    @Inject
    ICommandFactory commandFactory;

    @Inject
    JournalRepository journalRepository;

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
            LOG.infof("Attempting to recover command ID: %s (%s)",
                    entry.commandId,
                    entry.commandType);

            entry.payload.append(COMMANDS_PAYLOAD_KEY, entry.commands);

            ICommand<?> command = commandFactory
                    .buildCommand(
                            entry.commandType,
                            entry.payload);

            manager.dispatch(command);

            LOG.infof("Successfully recovered command ID: %s", entry.commandId);

        }
    }

}
