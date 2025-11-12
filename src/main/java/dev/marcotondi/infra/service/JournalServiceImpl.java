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
    JournalRepository journalRepository;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public List<JournalEntry> getAllEntries() {
        return journalRepository.listAll();
    }

    @Override
    public List<JournalEntry> getEntriesByCommandId(String commandId) {
        return journalRepository.findByCommandId(commandId);
    }

    @Override
    public JournalEntry createJournalEntry(CommandDescriptor<?> command, CommandStatus status) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Could not serialize command payload for command ID %s", command.commandId());
            // In a real-world scenario, you might want to rethrow a custom exception
            payload = "{\"error\":\"Payload serialization failed\"}";
        }

        JournalEntry entry = new JournalEntry(
                                command.commandId().toString(),
                                command.commandType(),
                                command.actor(),
                                payload,
                                LocalDateTime.now(),
                                status.name());


        journalRepository.persist(entry);

        return entry;
    }

    @Override
    public void updateJournalStatus(JournalEntry entry, CommandStatus status) {
        entry.setStatus(status.name());
        journalRepository.update(entry);
    }

    @Override
    public <R> void updateJournalOnSuccess(JournalEntry entry, R result, long duration) {
        entry.setEndTime(LocalDateTime.now());
        entry.setStatus(CommandStatus.COMPLETED.name());
        entry.setExecutionTimeMs(duration);
        try {
            String json = objectMapper.writeValueAsString(result);
            entry.setResult(json);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Error serializing result for command ID %s", entry.getCommandId());
            entry.setResult("{\"error\": \"Result serialization failed\"}");
        }
        journalRepository.update(entry);
    }

    @Override
    public void updateJournalOnFailure(JournalEntry entry, Exception e) {
        entry.setEndTime(LocalDateTime.now());
        entry.setStatus(CommandStatus.FAILED.name());
        entry.setErrorDetails(e.getClass().getName() + ": " + e.getMessage());
        journalRepository.update(entry);
    }
}
