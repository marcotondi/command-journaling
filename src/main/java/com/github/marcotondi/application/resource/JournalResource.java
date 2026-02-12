package com.github.marcotondi.application.resource;

import java.net.URI;
import java.util.List;

import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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

    @Inject
    ICommandManager manager;

    @Inject
    ICommandFactory commandFactory;

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

    @GET
    @Path("/sub-command/{subCommandId}")
    public Response getSubCommandId(@PathParam("subCommandId") String subCommandId) {

        return journalService.findBySubCommandId(subCommandId)
                .map(entry -> Response.ok(entry).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Journal entry not found for sub-command id: " + subCommandId)
                        .build());
    }

    public record CommandIdRequest(String commandId) {
    }

    @POST
    @Path("/restart")
    public Response restartByCommandId(CommandIdRequest request) {

        var journal = journalService.getJournalByCommandId(request.commandId);

        ICommand<?> command = commandFactory.buildCommand(
                journal.commandType(),
                journal.payload());

        manager.dispatchAsync(command);

        return Response.accepted()
                .location(URI.create("/api/journal/" + command.getDescriptor().getCommandId()))
                .build();
    }
}
