package dev.marcotondi.domain.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import dev.marcotondi.application.model.CompositeCommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.domain.exception.CommandExecutionException;
import dev.marcotondi.infra.CommandManager;
import dev.marcotondi.infra.JournalService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * A command that is composed of a list of other commands, implementing the Composite pattern.
 * It implements the Command interface, allowing it to be treated like any other command.
 * The execute and undo methods delegate the calls to the child commands and provide
 * transactional behavior for execute and undo operations.
 *
 */
@ApplicationScoped
public class CompositeCommand implements Command<Void> {
    private static final Logger LOG = Logger.getLogger(CompositeCommand.class);

    private CompositeCommandDescriptor descriptor;
    private final List<Command<?>> children = new ArrayList<>();

    @Inject
    CommandManager commandManager;

    @Inject
    JournalService journalService;

    public CompositeCommand() { }

    public CompositeCommand withDescriptor(CompositeCommandDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public CompositeCommand addCommand(Command<?> command) {
        this.children.add(command);
        return this;
    }

    @Override
    public CompositeCommandDescriptor getDescriptor() {
        if (descriptor == null) {
            // Genera il descrittore dai comandi figli se non già impostato
            List<CommandDescriptor> childDescriptors = children.stream()
                    .map(Command::getDescriptor)
                    .toList();

            descriptor = new CompositeCommandDescriptor(
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    "system",
                    "CompositeCommand",
                    childDescriptors);
        }
        return descriptor;
    }

    @Override
    @Transactional
    public Void execute() {
        LOG.infof("Executing composite command: %s with %d children",
                descriptor.commandId(), children.size());

        // Esegui ogni comando figlio in sequenza
        for (int i = 0; i < children.size(); i++) {
            Command<?> child = children.get(i);
            try {
                LOG.debugf("Executing child command %d/%d: %s",
                        i + 1, children.size(), child.getDescriptor().commandType());

                // Esegui il comando figlio tramite il CommandManager per assicurare il journaling
                CommandDescriptor childDescriptor = child.getDescriptor();
                JournalEntry childEntry = journalService.getEntriesByCommandId(childDescriptor.commandId().toString());
                commandManager.dispatch(child, childEntry);

                LOG.debugf("Child command %d completed successfully", i + 1);

            } catch (Exception e) {
                LOG.errorf(e, "Child command %d failed: %s",
                        i + 1, child.getDescriptor().commandType());

                // Rollback dei comandi già eseguiti
                rollbackExecutedChildren(i);

                throw new CommandExecutionException(
                        String.format("Composite command failed at child %d/%d", i + 1, children.size()), e);
            }
        }

        return null;
    }

    @Override
    public Void undo() {
        LOG.infof("Undoing composite command: %s", descriptor.commandId());

        // Undo in ordine inverso
        for (int i = children.size() - 1; i >= 0; i--) {
            Command<?> child = children.get(i);
            try {
                LOG.debugf("Undoing child command %d: %s",
                        i + 1, child.getDescriptor().commandType());
                child.undo();
            } catch (Exception e) {
                LOG.errorf(e, "Failed to undo child command %d", i + 1);
                // Continua con l'undo degli altri comandi
            }
        }

        return null;
    }

    private void rollbackExecutedChildren(int failedIndex) {
        LOG.warnf("Rolling back %d executed child commands", failedIndex);

        // Undo in ordine inverso fino al comando fallito (escluso)
        for (int i = failedIndex - 1; i >= 0; i--) {
            try {
                children.get(i).undo();
            } catch (Exception e) {
                LOG.errorf(e, "Failed to rollback child command %d during composite rollback", i + 1);
            }
        }
    }
}
