package dev.marcotondi.journal.infra.service;

import java.time.LocalDateTime;
import java.util.Map;

import dev.marcotondi.core.domain.CommandTypeName;

public interface StatisticsService {
    Map<String, Integer> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate);

    Double getAverageExecutionTime(CommandTypeName commandType);
}
