package com.github.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.entity.JournalEntity;
import com.github.marcotondi.core.repository.JournalRepository;
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
            CommandDescriptor descriptor) {

        return findByCommandId(descriptor.getCommandId().toString())
                .orElseGet(() -> createJournalEntity(descriptor, null));
    }

    @Override
    public JournalEntity getOrCreateEntry(
            CommandDescriptor descriptor,
            CommandDescriptor[] commandDescriptors) {

        return findByCommandId(descriptor.getCommandId().toString())
                .orElseGet(() -> createJournalEntity(descriptor, commandDescriptors));
    }

    @Override
    public JournalEntity createJournalEntity(
            CommandDescriptor descriptor,
            CommandDescriptor[] commandDescriptors) {

        String commandId = descriptor.getCommandId().toString();
        String typeName = descriptor.getCommandType();

        Document payload = serializePayload(descriptor, commandId);
        List<Document> commands = serializePayload(commandDescriptors, commandId);

        JournalEntity entry = new JournalEntity(
                commandId,
                typeName,
                payload,
                commands,
                LocalDateTime.now(),
                CommandStatus.PENDING.name());

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
        updateEntry(entry, CommandStatus.COMPLETED, null, durationMs,
                serializeResult(entry.commandId, result), null);
    }

    @Override
    public void updateJournalOnFailure(
            JournalEntity entry,
            Exception e) {
        updateEntry(entry, CommandStatus.FAILED, null, null, null, e.getMessage());
    }

    @Override
    public <R> void updateJournalOnRollBack(
            JournalEntity entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.ROLLED_BACK, null, durationMs,
                serializeResult(entry.commandId, result), null);
    }

    @Override
    public void updateJournalStatus(
            JournalEntity entry,
            CommandStatus status) {
        updateEntry(entry, status, null, null, null, null);
    }

    @Override
    public <R> void updateJournalPayload(
            JournalEntity entry,
            CommandDescriptor descriptor) {

        updateEntry(
                entry,
                CommandStatus.EXECUTING,
                serializePayload(descriptor, entry.commandId),
                null, null, null);
    }

    private void updateEntry(
            JournalEntity entry,
            CommandStatus status,
            Document payload,
            Long durationMs,
            Document result,
            String errorMessage) {

        entry.status = status.name();
        entry.endTime = LocalDateTime.now();

        if (payload != null) {
            entry.payload = payload;
        }
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
            Map<String, Object> map = objectMapper.convertValue(
                    descriptor,
                    new TypeReference<Map<String, Object>>() {
                    });

            return new Document(map);

        } catch (Exception e) {
            LOG.errorf(e, "Payload serialization failed for command %s", commandId);
            throw new RuntimeException("Cannot serialize payload for " + commandId, e);
        }
    }

    private List<Document> serializePayload(CommandDescriptor[] array, String commandId) {
        try {
            List<Document> descriptors = objectMapper.convertValue(
                    array,
                    new TypeReference<List<Document>>() {
                    });

            return (descriptors == null || descriptors.isEmpty()) ?
                    Collections.emptyList() :
                    descriptors;

        } catch (Exception e) {
            LOG.errorf(e, "Payload serialization failed for command %s", commandId);
            throw new RuntimeException("Cannot serialize payload for " + commandId, e);
        }
    }

    private <R> Document serializeResult(String commandId, R result) {
        try {
            // Se result è una stringa semplice, wrappala
            if (result instanceof String) {
                return new Document("data", result);
            }

            // Se è un array o Collection, wrappalo
            if (result instanceof Object[] || result instanceof Collection) {
                return new Document("data", result);
            }

            // Altrimenti serializza come JSON e parsalo
            Map<String, Object> map = objectMapper.convertValue(result,
                    new TypeReference<Map<String, Object>>() {});
            return new Document(map);

        } catch (Exception e) {
            LOG.errorf(e, "Result serialization failed for command %s", commandId);
            return new Document("error", "result serialization failed");
        }
    }
}
