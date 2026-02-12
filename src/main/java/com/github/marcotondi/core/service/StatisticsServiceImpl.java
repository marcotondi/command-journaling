package com.github.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.StatisticsService;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public Map<String, Integer> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        List<JournalEntity> entries = JournalEntity
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
        List<JournalEntity> entries = JournalEntity.find(CoreKeys.COMMAND_TYPE, commandType).list();

        return entries.stream()
                .filter(entry -> entry.executionTimeMs != null)
                .mapToLong(entry -> entry.executionTimeMs)
                .average()
                .orElse(0.0); // Return 0.0 if no entries or no execution times
    }
}
