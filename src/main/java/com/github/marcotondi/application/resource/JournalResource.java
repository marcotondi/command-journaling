package com.github.marcotondi.application.resource;

import java.net.URI;
import java.util.List;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.api.JournalService;
import com.github.marcotondi.core.entity.JournalEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/journal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JournalResource {

    private static final List<CommandStatus> RESTARTABLE = List.of(
            CommandStatus.FAILED,
            CommandStatus.PENDING);

    @Inject
    JournalService journalService;

    @Inject
    ICommandManager manager;

    @Inject
    ICommandFactory commandFactory;

    @GET
    public List<JournalEntity> getAllEntries(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return journalService.getAllEntries(safePage, safeSize);
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

        var journal = journalService.findByCommandId(request.commandId).orElse(null);
        if (journal == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Journal entry not found for id: " + request.commandId)
                    .build();
        }

        if (!journalService.claim(journal.commandId, RESTARTABLE)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Command " + journal.commandId + " is " + journal.status
                            + " and cannot be restarted")
                    .build();
        }

        journal.payload.append(CoreKeys.COMMANDS_PAYLOAD_KEY, journal.commands);

        ICommand<?> command = commandFactory.buildCommand(
                journal.commandType,
                journal.payload);

        manager.dispatchAsync(command);

        return Response.accepted()
                .location(URI.create("/api/journal/" + command.getDescriptor().getCommandId()))
                .build();
    }
}
