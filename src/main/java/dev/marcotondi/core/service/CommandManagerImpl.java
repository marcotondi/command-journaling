package dev.marcotondi.core.service;

import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandManager;
import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.domain.exception.CommandExecutionException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandManagerImpl implements ICommandManager {
    private static final Logger LOG = Logger.getLogger(CommandManagerImpl.class);

    @Override
    public <R> CompletableFuture<R> dispatchAsync(ICommand<R> command) {

        return CompletableFuture.supplyAsync(() -> dispatch(command));
    }

    @Override
    public <R> R dispatch(ICommand<R> command) {

        final CommandDescriptor descriptor = command.getDescriptor();
        try {
            LOG.debugf("Lanch execute command in transaction: %s", descriptor.getCommandType());

            R result = command.execute();

            return result;

        } catch (Exception e) {
            LOG.errorf(e, "Command execution failed for command ID %s", descriptor.getCommandId());
            throw new CommandExecutionException(
                    "Failed to execute command: " + descriptor.getCommandId(), e);
        }
    }


}
