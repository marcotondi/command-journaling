package dev.marcotondi.service;

import java.time.LocalDateTime;
import java.util.Map;

import dev.marcotondi.infra.repository.JournalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsService {

    @Inject
    JournalRepository journalRepository;

    public Map<String, Integer> getCommandStatistics(LocalDateTime from, LocalDateTime to) {
        return journalRepository.getCommandStatistics(from, to);
    }

    public Double getAverageExecutionTime(String commandType) {
        return journalRepository.getAverageExecutionTime(commandType);
    }
}
