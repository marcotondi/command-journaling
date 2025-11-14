package dev.marcotondi.journal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandStatus;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.Payload;
import dev.marcotondi.core.domain.PayloadMapper;
import dev.marcotondi.journal.api.JournalService;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.journal.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JournalServiceImpl implements JournalService {

    private static final Logger LOG = Logger.getLogger(JournalServiceImpl.class);

    @Inject
    JournalRepository repository;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    PayloadMapper payloadMapper;

    @Override
    public JournalEntry createJournalEntry(CommandDescriptor descriptor, CommandStatus status) {
        // 1. Get payload DTO from descriptor
        Payload payloadDto = payloadMapper.toPayload(descriptor);

        // 2. Serialize DTO to JSON string
        String payloadJson;
        try {
            // payloadDto can be null for commands without a payload (like CompositeCommand)
            payloadJson = payloadDto != null ? objectMapper.writeValueAsString(payloadDto) : null;
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Could not serialize command payload for command ID %s", descriptor.commandId());
            // Fail fast if serialization fails, as recovery would be impossible.
            throw new RuntimeException("Payload serialization failed for command ID " + descriptor.commandId(), e);
        }

        // 3. Get CommandTypeName from descriptor and set version
        CommandTypeName typeName = descriptor.commandType();
        int payloadVersion = 1; // For now, all payloads are version 1

        // 4. Create and persist the JournalEntry
        JournalEntry entry = new JournalEntry(
                descriptor.commandId().toString(),
                typeName,
                payloadVersion,
                descriptor.actor(),
                payloadJson,
                LocalDateTime.now(),
                status.name());

        repository.persist(entry);
        return entry;
    }

    @Override
    public void linkChildToParent(JournalEntry child, JournalEntry parent) {
        child.parentCommandId = parent.commandId;

        repository.update(child);

        // Aggiorna anche il parent
        if (!parent.childCommandIds.contains(child.commandId)) {
            parent.childCommandIds.add(child.commandId);
            repository.update(parent);
        }
    }

    @Override
    public void updateJournalStatus(JournalEntry entry, CommandStatus status) {
        entry.status = status.name();
        repository.update(entry);
    }

    @Override
    public <R> void updateJournalOnSuccess(
            JournalEntry entry,
            R result,
            long durationMs) {

        entry.endTime = LocalDateTime.now();
        entry.status = CommandStatus.COMPLETED.name();
        entry.executionTimeMs = durationMs;

        try {
            entry.result = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Error serializing result for command ID %s", entry.commandId);
            entry.result = "{\"error\": \"Result serialization failed\"}";
        }
        repository.update(entry);

        // If it is a composite, also update the status of the children
        if (entry.isComposite()) {
            updateChildrenStatus(entry.commandId, CommandStatus.COMPLETED);
        }
    }

    @Override
    public void updateJournalOnFailure(JournalEntry entry, Exception e) {
        entry.status = CommandStatus.FAILED.name();
        entry.endTime = LocalDateTime.now();
        entry.errorMessage = e.getMessage();

        repository.update(entry);

        // If it is a composite, mark the children as rolled back
        if (entry.isComposite()) {
            updateChildrenStatus(entry.commandId, CommandStatus.ROLLED_BACK);
        }
    }

    private void updateChildrenStatus(String parentCommandId, CommandStatus status) {
        List<JournalEntry> children = getChildEntries(parentCommandId);
        for (JournalEntry child : children) {
            updateJournalStatus(child, status);
            if (child.isComposite()) {
                updateChildrenStatus(child.commandId, status);
            }
        }
    }

    @Override
    public List<JournalEntry> getAllEntries() {
        return repository.listAll();
    }

    @Override
    public JournalEntry getEntriesByCommandId(String commandId) {
        return repository.findByCommandId(commandId);
    }

    @Override
    public List<JournalEntry> getChildEntries(String parentCommandId) {
        // Delega la query al repository
        return repository.findChildEntries(parentCommandId);
    }

    @Override
    public JournalEntry getParentEntry(String childCommandId) {
        // Delega la query al repository
        return repository.findParentEntry(childCommandId);
    }
}
