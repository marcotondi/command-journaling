package dev.marcotondi.application.resource;

import java.util.List;

import dev.marcotondi.core.api.JournalService;
import dev.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/journal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JournalResource {

    @Inject
    JournalService journalService;

    @GET
    public List<JournalEntity> getAllEntries() {
        return journalService.getAllEntries();
    }

    @GET
    @Path("/{commandId}")
    public Response getEntryByCommandId(@PathParam("commandId") String commandId) {

        return journalService.findByCommandId(commandId)
                .map(entry -> Response.ok(entry).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Journal entry not found for id: " + commandId)
                        .build());
    }
}
