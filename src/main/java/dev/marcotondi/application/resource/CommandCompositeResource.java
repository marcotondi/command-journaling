package dev.marcotondi.application.resource;

import java.net.URI;
import java.util.List;

import dev.marcotondi.application.CommandDescriptorFactory;
import dev.marcotondi.application.CommandRequest;
import dev.marcotondi.domain.api.CompositeCommandDescriptor;
import dev.marcotondi.infra.CommandManager;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/commands/composite")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandCompositeResource {

    @Inject
    CommandManager dispatcher;

    @Inject
    CommandDescriptorFactory descriptorFactory;

    // This record defines the structure of the JSON request body for this endpoint.
    public record CompositeCommandRequest(String actor, List<CommandRequest> commands) {}

    @POST
    public Response executeCompositeCommand(@Valid CompositeCommandRequest request) {
        
        CompositeCommandDescriptor<?> compositeCommand = descriptorFactory.createComposite(request.actor(), request.commands());

        dispatcher.dispatchAsync(compositeCommand);

        return Response.accepted()
            .location(URI.create("/api/journal/" + compositeCommand.commandId()))
            .build();
    }
}
