package dev.marcotondi.application.resource;

import java.util.List;

import dev.marcotondi.domain.entity.JournalEntry;
import dev.marcotondi.infra.JournalService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/journal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JournalResource {

    @Inject
    JournalService journalService;

    @GET
    public List<JournalEntry> getAllEntries() {
        return journalService.getAllEntries();
    }

    @GET
    @Path("/{commandId}")
    public JournalEntry getEntriesByCommandId(@PathParam("commandId") String commandId) {
        return journalService.getEntriesByCommandId(commandId);
    }
}
