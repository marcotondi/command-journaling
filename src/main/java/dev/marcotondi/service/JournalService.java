package dev.marcotondi.service;

import java.util.List;

import dev.marcotondi.domain.entry.JournalEntry;
import dev.marcotondi.infra.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JournalService {

    @Inject
    JournalRepository journalRepository;

    public List<JournalEntry> getAllEntries() {
        return journalRepository.listAll();
    }

    public List<JournalEntry> getEntriesByCommandId(String commandId) {
        return journalRepository.findByCommandId(commandId);
    }
}
