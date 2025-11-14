package dev.marcotondi.journal.api;

import java.util.List;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandStatus;
import dev.marcotondi.journal.domain.JournalEntry;

public interface JournalService {

    JournalEntry createJournalEntry(CommandDescriptor descriptor, CommandStatus status);

    void linkChildToParent(JournalEntry child, JournalEntry parent);

    void updateJournalStatus(JournalEntry entry, CommandStatus status);

    <R> void updateJournalOnSuccess(JournalEntry entry, R result, long durationMs);

    void updateJournalOnFailure(JournalEntry entry, Exception e);

    List<JournalEntry> getAllEntries();

    JournalEntry getEntriesByCommandId(String commandId);

    List<JournalEntry> getChildEntries(String parentCommandId);

    JournalEntry getParentEntry(String childCommandId);


}
