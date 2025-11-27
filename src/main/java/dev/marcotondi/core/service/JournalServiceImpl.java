package dev.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.CommandStatus;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.JournalService;
import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.entity.JournalEntity;
import dev.marcotondi.core.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JournalServiceImpl implements JournalService {
    private static final Logger LOG = Logger.getLogger(JournalServiceImpl.class);

    @Inject
    JournalRepository repository;

    @Inject
    ObjectMapper objectMapper;

    // ------------------------------------------------------------
    // ENTRY CREATION
    // ------------------------------------------------------------

    @Override
    public JournalEntity getOrCreateEntry(
            CommandDescriptor descriptor,
            CommandStatus initialStatus) {

        return findByCommandId(descriptor.getCommandId().toString())
                .orElseGet(() -> createJournalEntity(descriptor, initialStatus));
    }

    @Override
    public JournalEntity createJournalEntity(
            CommandDescriptor descriptor,
            CommandStatus status) {

        String commandId = descriptor.getCommandId().toString();
        CommandTypeName typeName = descriptor.getCommandType();

        Document document = serializePayload(descriptor, commandId);

        JournalEntity entry = new JournalEntity(
                commandId,
                typeName,
                document,
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
            JournalEntity entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.COMPLETED, durationMs, serializeResult(entry.commandId, result), null);
    }

    @Override
    public void updateJournalOnFailure(
            JournalEntity entry,
            Exception e) {
        updateEntry(entry, CommandStatus.FAILED, null, null, e.getMessage());
    }

    @Override
    public <R> void updateJournalOnRollBack(
            JournalEntity entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.ROLLED_BACK, durationMs, serializeResult(entry.commandId, result), null);
    }

    @Override
    public void updateJournalStatus(
            JournalEntity entry,
            CommandStatus status) {
        updateEntry(entry, status, null, null, null);
    }

    private void updateEntry(
            JournalEntity entry,
            CommandStatus status,
            Long durationMs,
            Document result,
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
    public Optional<JournalEntity> findByCommandId(String commandId) {
        return repository.findByCommandId(commandId);
    }

    @Override
    public List<JournalEntity> getAllEntries() {
        return repository.listAll();
    }

    // ------------------------------------------------------------
    // INTERNAL UTILS
    // ------------------------------------------------------------

    private Document serializePayload(CommandDescriptor descriptor, String commandId) {
        try {
            var json = objectMapper.writeValueAsString(descriptor);

            return Document.parse(json);
        } catch (Exception e) {
            LOG.errorf(e, "Payload serialization failed for command %s", commandId);
            throw new RuntimeException("Cannot serialize payload for " + commandId, e);
        }
    }

    private <R> Document serializeResult(String commandId, R result) {
        try {
            // Se result è una stringa semplice, wrappala
            if (result instanceof String) {
                return new Document("msg", result);
            }

            // Se è un array o Collection, wrappalo
            if (result instanceof Object[] || result instanceof Collection) {
                return new Document("data", result);
            }

            // Altrimenti serializza come JSON e parsalo
            return Document.parse(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            LOG.errorf(e, "Result serialization failed for command %s", commandId);
            return new Document("error", "result serialization failed");
        }
    }
}
