package dev.marcotondi.api;

import dev.marcotondi.service.StatisticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDateTime;
import java.util.Map;

@Path("/api/statistics")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    @Inject
    StatisticsService statisticsService;

    @GET
    @Path("/commands")
    public Map<String, Integer> getStatistics(
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : LocalDateTime.now().minusDays(7);
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : LocalDateTime.now();
        return statisticsService.getCommandStatistics(fromDate, toDate);
    }

    @GET
    @Path("/avg-time/{commandType}")
    public Double getAverageTime(@PathParam("commandType") String commandType) {
        return statisticsService.getAverageExecutionTime(commandType);
    }
}