package com.github.marcotondi.core.api;

import java.util.List;
import java.util.Optional;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.entity.JournalEntity;

public interface JournalService {

    JournalEntity getOrCreateEntry(CommandDescriptor descriptor);

    JournalEntity getOrCreateEntry(CommandDescriptor descriptor, CommandDescriptor[] commandDescriptors);

    JournalEntity createJournalEntity(CommandDescriptor descriptor, CommandDescriptor[] commandDescriptors);

    <R> void updateJournalOnSuccess(JournalEntity entry, R result, long durationMs);

    void updateJournalOnFailure(JournalEntity entry, Exception e);

    <R> void updateJournalOnRollBack(JournalEntity entry, R result, long durationMs);

    void updateJournalStatus(JournalEntity entry, CommandStatus status);

    <R> void updateJournalPayload(JournalEntity entry, CommandDescriptor descriptor);

    Optional<JournalEntity> findByCommandId(String commandId);

    List<JournalEntity> getAllEntries();

}
