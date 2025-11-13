package dev.marcotondi.infra.service;

import java.time.LocalDateTime;
import java.util.Map;

import dev.marcotondi.domain.api.CommandTypeName;
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

    public Double getAverageExecutionTime(CommandTypeName commandType) {
        return journalRepository.getAverageExecutionTime(commandType);
    }
}
