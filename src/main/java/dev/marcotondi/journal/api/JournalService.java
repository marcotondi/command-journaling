package dev.marcotondi.journal.api;

import java.util.List;
import java.util.Optional;

import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.journal.domain.JournalEntry;

public interface JournalService {

    JournalEntry getOrCreateEntry(CommandDescriptor descriptor, CommandStatus initialStatus);

    JournalEntry createJournalEntry(CommandDescriptor descriptor, CommandStatus status);

    <R> void updateJournalOnSuccess(JournalEntry entry, R result, long durationMs);

    void updateJournalOnFailure(JournalEntry entry, Exception e);

    <R> void updateJournalOnRollBack(JournalEntry entry, R result, long durationMs);

    void updateJournalStatus(JournalEntry entry, CommandStatus status);

    Optional<JournalEntry> findByCommandId(String commandId);

	List<JournalEntry> getAllEntries();

}
