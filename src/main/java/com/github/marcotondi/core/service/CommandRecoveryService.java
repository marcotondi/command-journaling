package com.github.marcotondi.core.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.entity.JournalEntity;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
class CommandRecoveryService {
    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    @Inject
    ICommandManager manager;
    @Inject
    ICommandFactory commandFactory;

    void onStart(@Observes @Priority(Integer.MAX_VALUE) StartupEvent ev) {
        LOG.info("Starting recovery of interrupted commands...");
        recoverInterruptedCommands();
    }

    private void recoverInterruptedCommands() {
        List<JournalEntity> interrupted = JournalEntity.findInterruptedCommands();
        if (interrupted.isEmpty()) {
            LOG.info("No interrupted commands found. Recovery not needed.");
            return;
        }

        LOG.infof("Found %d interrupted commands to recover.", interrupted.size());

        for (JournalEntity entry : interrupted) {
            LOG.infof("Attempting to recover command ID: %s (%s)",
                    entry.commandId,
                    entry.commandType);

            entry.payload.append(CoreKeys.COMMANDS_PAYLOAD_KEY, entry.commands);

            try {
                ICommand<?> command = commandFactory
                        .buildCommand(
                                entry.commandType,
                                entry.payload);

                manager.dispatch(command);
                LOG.infof("Successfully recovered command ID: %s", entry.commandId);

            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command ID: %s", entry.commandId);
            }
        }
    }

}
