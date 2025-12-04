package dev.marcotondi.core.api;

import java.util.List;
import java.util.Optional;

import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.entity.JournalEntity;

public interface JournalService {

    JournalEntity getOrCreateEntry(CommandDescriptor descriptor, CommandStatus initialStatus);

    JournalEntity createJournalEntity(CommandDescriptor descriptor, CommandStatus status);

    <R> void updateJournalOnSuccess(JournalEntity entry, R result, long durationMs);

    void updateJournalOnFailure(JournalEntity entry, Exception e);

    <R> void updateJournalOnRollBack(JournalEntity entry, R result, long durationMs);

    void updateJournalStatus(JournalEntity entry, CommandStatus status);

    <R> void updateJournalPayload(JournalEntity entry, CommandDescriptor descriptor);

    Optional<JournalEntity> findByCommandId(String commandId);

    List<JournalEntity> getAllEntries();

}
