package dev.marcotondi.journal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.api.CommandDescriptor;
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

    // ------------------------------------------------------------
    // ENTRY CREATION
    // ------------------------------------------------------------

    @Override
    public JournalEntry getOrCreateEntry(
            CommandDescriptor descriptor,
            CommandStatus initialStatus) {

        return findByCommandId(descriptor.commandId().toString())
                .orElseGet(() -> createJournalEntry(descriptor, initialStatus));
    }

    @Override
    public JournalEntry createJournalEntry(
            CommandDescriptor descriptor,
            CommandStatus status) {

        String commandId = descriptor.commandId().toString();
        CommandTypeName typeName = descriptor.commandType();
        int version = Payload.version;

        String payloadJson = serializePayload(descriptor, commandId);

        JournalEntry entry = new JournalEntry(
                commandId,
                typeName,
                version,
                descriptor.actor(),
                payloadJson,
                LocalDateTime.now(),
                status.name());

        repository.persist(entry);
        return entry;
    }

    // ------------------------------------------------------------
    // STATE TRANSITION HELPERS
    // ------------------------------------------------------------

    @Override
    public <R> void updateJournalOnSuccess(
            JournalEntry entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.COMPLETED, durationMs, serializeResult(entry, result), null);
    }

    @Override
    public void updateJournalOnFailure(
            JournalEntry entry,
            Exception e) {
        updateEntry(entry, CommandStatus.FAILED, null, null, e.getMessage());
    }

    @Override
    public <R> void updateJournalOnRollBack(
            JournalEntry entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.ROLLED_BACK, durationMs, serializeResult(entry, result), null);
    }

    @Override
    public void updateJournalStatus(
            JournalEntry entry,
            CommandStatus status) {
        updateEntry(entry, status, null, null, null);
    }

    private void updateEntry(
            JournalEntry entry,
            CommandStatus status,
            Long durationMs,
            String result,
            String errorMessage) {

        entry.status = status.name();
        entry.endTime = LocalDateTime.now();

        if (durationMs != null) {
            entry.executionTimeMs = durationMs;
        }
        if (result != null) {
            entry.result = result;
        }
        if (errorMessage != null) {
            entry.errorMessage = errorMessage;
        }

        repository.update(entry);
    }

    // ------------------------------------------------------------
    // QUERIES
    // ------------------------------------------------------------

    @Override
    public Optional<JournalEntry> findByCommandId(String commandId) {
        return repository.findByCommandId(commandId);
    }

    @Override
    public List<JournalEntry> getAllEntries() {
        return repository.listAll();
    }

    // ------------------------------------------------------------
    // INTERNAL UTILS
    // ------------------------------------------------------------

    private String serializePayload(CommandDescriptor descriptor, String commandId) {
        try {
            Payload dto = payloadMapper.toPayload(descriptor);
            return dto != null ? objectMapper.writeValueAsString(dto) : null;
        } catch (Exception e) {
            LOG.errorf(e, "Payload serialization failed for command %s", commandId);
            throw new RuntimeException("Cannot serialize payload for " + commandId, e);
        }
    }

    private <R> String serializeResult(JournalEntry entry, R result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            LOG.errorf(e, "Result serialization failed for command %s", entry.commandId);
            return "{\"error\": \"result serialization failed\"}";
        }
    }
}
