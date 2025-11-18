package dev.marcotondi.application.resource;

import java.time.LocalDateTime;
import java.util.Map;

import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.api.StatisticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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


    public record AverageTimeResponse(String commandType, String averageTime) {}

    @GET
    @Path("/avg-time/{commandType}")
    public Response getAverageTime(@PathParam("commandType") String commandTypeString) {

        CommandTypeName commandType = CommandTypeName.valueOf(commandTypeString.toUpperCase());
        String avg = statisticsService.getAverageExecutionTime(commandType) + " ms";
        AverageTimeResponse response = new AverageTimeResponse(commandTypeString, avg);

        return Response.ok()
                .entity(response)
                .build();
    }
}
