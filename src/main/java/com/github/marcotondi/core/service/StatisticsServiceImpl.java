package com.github.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.github.marcotondi.core.api.StatisticsService;
import com.github.marcotondi.core.entity.JournalEntity;
import com.github.marcotondi.core.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsServiceImpl implements StatisticsService {

    @Inject
    JournalRepository journalRepository;

    @Override
    public Map<String, Integer> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        List<JournalEntity> entries = journalRepository
            .find("startTime >= ?1 and startTime <= ?2", fromDate, toDate)
            .list();

        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.commandType,
                        Collectors.summingInt(entry -> 1)
                ));
    }

    @Override
    public Double getAverageExecutionTime(String commandType) {
        List<JournalEntity> entries = journalRepository.find("commandType", commandType).list();

        return entries.stream()
                .filter(entry -> entry.executionTimeMs != null)
                .mapToLong(entry -> entry.executionTimeMs)
                .average()
                .orElse(0.0); // Return 0.0 if no entries or no execution times
    }
}
