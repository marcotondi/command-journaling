package dev.marcotondi.infra;

import java.util.List;

import dev.marcotondi.domain.CommandStatus;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;

public interface JournalService {

    public List<JournalEntry> getAllEntries();

    public List<JournalEntry> getEntriesByCommandId(String commandId);

    public JournalEntry createJournalEntry(CommandDescriptor<?> command, CommandStatus status);

    public void updateJournalStatus(JournalEntry entry, CommandStatus status);

    public <R> void updateJournalOnSuccess(JournalEntry entry, R result, long duration);

    public void updateJournalOnFailure(JournalEntry entry, Exception e);
}
