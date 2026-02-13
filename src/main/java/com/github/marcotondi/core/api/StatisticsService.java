package com.github.marcotondi.core.api;

import java.time.LocalDateTime;
import java.util.Map;

public interface StatisticsService {
    Map<String, Long> getCommandStatistics(LocalDateTime fromDate, LocalDateTime toDate);

    Double getAverageExecutionTime(String commandType);
}
