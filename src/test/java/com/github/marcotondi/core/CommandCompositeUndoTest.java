package com.github.marcotondi.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.marcotondi.application.composite.SimpleCompositeDescriptor;
import com.github.marcotondi.application.sleep.model.SleepDescriptor;
import com.github.marcotondi.application.user.model.CreateUserDescriptor;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.domain.CommandDescriptor;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class CommandCompositeUndoTest {

    @Inject
    ICommandManager manager;

    @Inject
    ICommandFactory commandFactory;

    @Inject
    JournalService journalService;

    @Test
    void compositeUndoRollsBackSubCommandsAndJournalsTheResult() {
        ICommand<List<?>> composite = commandFactory.buildGroupCommand(
                new SimpleCompositeDescriptor(),
                new CommandDescriptor[] {
                        new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1),
                        new CreateUserDescriptor(CoreKeys.SYSTEM_ACTOR, "undo-user", "undo-user@test.dev")
                });

        manager.dispatch(composite);

        List<?> undone = composite.undo();
        assertEquals(2, undone.size());

        String commandId = composite.getDescriptor().getCommandId();
        var entry = journalService.findByCommandId(commandId).orElse(null);
        assertNotNull(entry, "journal entry must exist after rollback");
        assertEquals(CommandStatus.ROLLED_BACK, entry.status);
    }

    @Test
    void compositeSubCommandsDoNotAccumulateAcrossExecutions() {
        for (int i = 0; i < 2; i++) {
            ICommand<List<?>> composite = commandFactory.buildGroupCommand(
                    new SimpleCompositeDescriptor(),
                    new CommandDescriptor[] {
                            new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1),
                            new CreateUserDescriptor(CoreKeys.SYSTEM_ACTOR, "undo-user", "undo-user@test.dev")
                    });

            manager.dispatch(composite);

            var entry = journalService.findByCommandId(composite.getDescriptor().getCommandId()).orElse(null);
            assertNotNull(entry);
            assertEquals(2, entry.commands.size(), "sub-commands must not accumulate across executions");
        }
    }
}
