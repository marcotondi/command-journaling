package com.github.marcotondi.core.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import com.github.marcotondi.core.JournalResponse;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.domain.CommandDescriptor;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class JournalServiceImpl implements JournalService {
    private static final Logger LOG = Logger.getLogger(JournalServiceImpl.class);

    @Inject
    ObjectMapper objectMapper;

    // ------------------------------------------------------------
    // ENTRY CREATION
    // ------------------------------------------------------------

    @Override
    @Transactional
    public JournalEntity getOrCreateEntry(
            CommandDescriptor descriptor) {

        return findByCommandId(descriptor.getCommandId().toString())
                .orElseGet(() -> createJournalEntity(descriptor, null));
    }

    @Override
    @Transactional
    public JournalEntity getOrCreateEntry(
            CommandDescriptor descriptor,
            CommandDescriptor[] commandDescriptors) {

        return findByCommandId(descriptor.getCommandId().toString())
                .orElseGet(() -> createJournalEntity(descriptor, commandDescriptors));
    }

    @Override
    @Transactional
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
                LocalDateTime.now(ZoneOffset.UTC),
                CommandStatus.PENDING);

        entry.persist();
        return entry;
    }

    // ------------------------------------------------------------
    // STATE TRANSITION HELPERS
    // ------------------------------------------------------------

    @Override
    @Transactional
    public <R> void updateJournalOnSuccess(
            JournalEntity entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.COMPLETED, null, durationMs,
                serializeResult(entry.commandId, result), null);
    }

    @Override
    @Transactional
    public void updateJournalOnFailure(
            JournalEntity entry,
            Exception e) {
        updateEntry(entry, CommandStatus.FAILED, null, null, null, e.getMessage());
    }

    @Override
    @Transactional
    public <R> void updateJournalOnRollBack(
            JournalEntity entry,
            R result,
            long durationMs) {
        updateEntry(entry, CommandStatus.ROLLED_BACK, null, durationMs,
                serializeResult(entry.commandId, result), null);
    }

    @Override
    @Transactional
    public void updateJournalStatus(
            JournalEntity entry,
            CommandStatus status) {
        updateEntry(entry, status, null, null, null, null);
    }

    @Override
    @Transactional
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

        if (payload != null) {
            entry.payload = payload;
        }
        if (durationMs != null) {
            entry.executionTimeMs = durationMs;
        }
        if (result != null) {
            entry.result = result;
        }

        entry.status = status;
        entry.errorMessage = errorMessage;
        entry.endTime = LocalDateTime.now(ZoneOffset.UTC);

        entry.update();
    }

    // ------------------------------------------------------------
    // QUERIES
    // ------------------------------------------------------------

    @Override
    public Optional<JournalEntity> findByCommandId(String commandId) {
        return JournalEntity.findByCommandId(commandId);
    }

    @Override
    public Optional<JournalEntity> findBySubCommandId(String subCommandId) {
        return Optional.ofNullable(JournalEntity.findBySubCommandId(subCommandId));
    }

    @Override
    public List<JournalEntity> getAllEntries() {
        return JournalEntity.listAll();
    }

    @Override
    public JournalResponse getJournalByCommandId(String commandId) {
        var journal = JournalEntity.findByCommandId(commandId)
                .orElseThrow(() -> new NotFoundException("Command not found: " + commandId));

        return JournalResponse.from(journal);
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
        if (array == null) {
            LOG.warnf("No command descriptors provided for journal entry %s", commandId);
            return Collections.emptyList();
        }

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
            // Se è un array o Collection, wrappalo
            if (result instanceof String || result instanceof Object[] || result instanceof Collection) {
                return new Document("data", result);
            }

            if (result instanceof Map map) {
                return new Document(map);
            }

            // Altrimenti serializza come JSON e parsalo
            Map<String, Object> map = objectMapper.convertValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            return new Document(map);

        } catch (Exception e) {
            LOG.errorf(e, "Result serialization failed for command %s", commandId);
            throw new RuntimeException("Cannot serialize payload for " + commandId, e);
        }
    }
}
