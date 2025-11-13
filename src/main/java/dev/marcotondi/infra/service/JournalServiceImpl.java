package dev.marcotondi.infra.service;

import java.time.LocalDateTime;
import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.domain.CommandStatus;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.infra.JournalService;
import dev.marcotondi.infra.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JournalServiceImpl implements JournalService {

    private static final Logger LOG = Logger.getLogger(JournalServiceImpl.class);

    @Inject
    JournalRepository repository;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public JournalEntry createJournalEntry(CommandDescriptor descriptor, CommandStatus status) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(descriptor);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Could not serialize command payload for command ID %s", descriptor.commandId());
            // In a real-world scenario, you might want to rethrow a custom exception
            payload = "{\"error\":\"Payload serialization failed\"}";
        }

        // Store the fully qualified class name of the descriptor in the commandType field.
        // This is crucial for the recovery service to be able to reconstruct the descriptor.
        String descriptorClassName = descriptor.getClass().getName();

        JournalEntry entry = new JournalEntry(
                descriptor.commandId().toString(),
                descriptorClassName,
                descriptor.actor(),
                payload,
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
