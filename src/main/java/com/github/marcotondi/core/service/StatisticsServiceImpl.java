package com.github.marcotondi.core.service;

import java.time.LocalDateTime;
import java.util.Map;

import com.github.marcotondi.core.api.StatisticsService;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public Map<String, Long> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        return JournalEntity.getCommandStatistics(fromDate, toDate);
    }

    @Override
    public Double getAverageExecutionTime(String commandType) {
        return JournalEntity.getAverageExecutionTime(commandType);
    }
}
