package dev.marcotondi.journal.infra.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.marcotondi.core.domain.CommandTypeName;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.journal.infra.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsServiceImpl implements StatisticsService {

    @Inject
    JournalRepository journalRepository;

    @Override
    public Map<String, Integer> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        List<JournalEntry> entries = journalRepository.find("startTime BETWEEN ?1 and ?2", fromDate, toDate).list();

        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.commandType.name(),
                        Collectors.summingInt(entry -> 1)
                ));
    }

    @Override
    public Double getAverageExecutionTime(CommandTypeName commandType) {
        List<JournalEntry> entries = journalRepository.find("commandType", commandType).list();

        return entries.stream()
                .filter(entry -> entry.executionTimeMs != null)
                .mapToLong(entry -> entry.executionTimeMs)
                .average()
                .orElse(0.0); // Return 0.0 if no entries or no execution times
    }
}
