package dev.marcotondi.application.resource;

import java.net.URI;

import org.jboss.logging.Logger;

import dev.marcotondi.application.composite.SampleCompositeCommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.api.ICommandManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/commands/composite")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompositeResource {
    private static final Logger LOG = Logger.getLogger(CompositeResource.class);

    @Inject
    ICommandManager manager;

    @Inject
    ICommandFactory commandFactory;

    @POST
    public Response executeCompositeCommand() {
        LOG.info("Start composite command ");

        SampleCompositeCommand scc = new SampleCompositeCommand(commandFactory);

        manager.dispatch(scc);

        return Response.accepted()
                .location(URI.create("/api/journal/" + 1L))
                .build();
    }


}
