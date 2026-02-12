package com.github.marcotondi.core.service;

import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.domain.exception.CommandExecutionException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CommandManagerImpl implements ICommandManager {
    private static final Logger LOG = Logger.getLogger(CommandManagerImpl.class);

    @Inject
    ManagedExecutor executor;

    @Override
    public <R> CompletableFuture<R> dispatchAsync(ICommand<R> command) {

        return CompletableFuture.supplyAsync(() -> dispatch(command), executor);
    }

    @Override
    public <R> R dispatch(ICommand<R> command) {

        final CommandDescriptor descriptor = command.getDescriptor();
        try {
            LOG.debugf("Lanch execute command in transaction: %s", descriptor.getCommandType());

            R result = command.execute();

            return result;

        } catch (Exception e) {
            LOG.warnf(e, "Command execution failed. Call undo for command ID %s", descriptor.getCommandId());
            // command.undo();
            throw new CommandExecutionException(
                    "Failed to execute command: " + descriptor.getCommandId(), e);
        }
    }


}
