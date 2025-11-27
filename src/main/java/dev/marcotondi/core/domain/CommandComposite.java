package dev.marcotondi.core.domain;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.domain.exception.CommandExecutionException;

/**
 * Represents a composite command that executes a sequence of other commands.
 * <p>
 * A concrete implementation of this class should provide the list of
 * descriptors for the sub-commands. The result of the composite command's
 * execution is a list of the results of all sub-commands.
 *
 * @param <R> The type of the result of the sub-commands. The composite result
 *            will be a List<R>.
 */
public abstract class CommandComposite<R> implements ICommand<List<R>> {
    private static final Logger LOG = Logger.getLogger(CommandComposite.class);

    private final List<ICommand<R>> commands = new ArrayList<>();
    private final CompositeDescriptor descriptors;

    /**
     * Constructs a composite command.
     *
     * @param descriptors    The list of descriptors for the sub-commands to be
     *                       executed.
     * @param commandFactory The factory to create the command instances.
     */
    public CommandComposite(CompositeDescriptor descriptors) {
    this.descriptors = descriptors;
    }

    /**
     * Executes all sub-commands in sequence.
     * If any command fails, the execution stops and a CommandExecutionException is
     * thrown.
     *
     * @return A list containing the results of each successfully executed
     *         sub-command.
     * @throws CommandExecutionException if any of the sub-commands fail.
     */
    @Override
    public List<R> execute() throws CommandExecutionException {
        LOG.debugf("Execute CommandComposite");

        List<R> results = new ArrayList<>();
        for (ICommand<R> command : commands) {
            LOG.debugf("Executing command: %s", command.getDescriptor().getCommandType());

            try {
                results.add(command.execute());
            } catch (Exception e) {
                LOG.errorf(e, "Command execution failed for command ID %s", command.getDescriptor().getCommandId());

                throw new CommandExecutionException(
                        "Execution failed for sub-command " + command.getClass().getSimpleName(), e);
            }
        }
        return results;
    }

    @Override
    public List<R> undo() throws CommandExecutionException {
        List<R> results = new ArrayList<>();
        for (ICommand<R> command : commands) {
            LOG.debugf("Rollback command in: %s", command.getDescriptor().getCommandType());

            try {
                results.add(command.undo());
            } catch (Exception e) {
                LOG.errorf(e, "Undo-Command execution failed for command ID %s",
                        command.getDescriptor().getCommandId());

                throw new CommandExecutionException(
                        "Execution failed for sub-command " + command.getClass().getSimpleName(), e);
            }
        }
        return results;
    }

    @Override
    public CommandDescriptor getDescriptor() {
        return this.descriptors;
    }

}
