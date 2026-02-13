package com.github.marcotondi.core.service;

import java.util.List;

import org.jboss.logging.Logger;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.entity.JournalEntity;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
class CommandRecoveryService {
    private static final Logger LOG = Logger.getLogger(CommandRecoveryService.class);

    private static final List<CommandStatus> INTERRUPTED = List.of(
            CommandStatus.PENDING,
            CommandStatus.EXECUTING,
            CommandStatus.RECOVERING,
            CommandStatus.EXECUTING_ROLL_BACK);

    @Inject
    ICommandManager manager;
    @Inject
    ICommandFactory commandFactory;
    @Inject
    JournalService journal;

    void onStart(@Observes @Priority(Integer.MAX_VALUE) StartupEvent ev) {
        LOG.info("Starting recovery of interrupted commands...");
        recoverInterruptedCommands();
    }

    void recoverInterruptedCommands() {
        List<JournalEntity> interrupted = JournalEntity.findInterruptedCommands();
        if (interrupted.isEmpty()) {
            LOG.info("No interrupted commands found. Recovery not needed.");
            return;
        }

        LOG.infof("Found %d interrupted commands to recover.", interrupted.size());

        for (JournalEntity entry : interrupted) {
            // Claim atomically: with multiple replicas only one process may run the command.
            if (!journal.claim(entry.commandId, INTERRUPTED)) {
                LOG.infof("Command ID %s already claimed by another instance, skipping", entry.commandId);
                continue;
            }

            LOG.infof("Recovering command ID: %s (%s)", entry.commandId, entry.commandType);

            entry.payload.append(CoreKeys.COMMANDS_PAYLOAD_KEY, entry.commands);

            try {
                ICommand<?> command = commandFactory.buildCommand(
                        entry.commandType,
                        entry.payload);

                manager.dispatchAsync(command).exceptionally(ex -> {
                    LOG.errorf(ex, "Recovery failed for command ID: %s", entry.commandId);
                    return null;
                });
            } catch (Exception e) {
                LOG.errorf(e, "Failed to recover command ID: %s", entry.commandId);
            }
        }
    }

}
