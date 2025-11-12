package dev.marcotondi.application.resource;

import java.net.URI;

import dev.marcotondi.application.model.CreateUserDescriptor;
import dev.marcotondi.application.model.DeleteUserDescriptor;
import dev.marcotondi.infra.CommandManager;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandResource {

    @Inject
    CommandManager dispatcher;

    // Record for the request payload
    public record CreateUserRequest(String username, String email, String actor) {}

    @POST
    @Path("/users/create")
    public Response createUser(@Valid CreateUserRequest request) {
        var descriptor = new CreateUserDescriptor(request.actor(), request.username(), request.email());
        dispatcher.dispatch(descriptor);
        // Return 202 Accepted to indicate the command has been accepted for processing.
        // The location header can point to a resource to check the command's status.
        return Response.accepted()
            .location(URI.create("/api/journal/" + descriptor.commandId()))
            .build();
    }

    // Record for the request payload
    public record DeleteUserRequest(String email, String actor) {}

    @POST
    @Path("/users/delete")
    public Response deleteUser(@Valid DeleteUserRequest request) {
        var descriptor = new DeleteUserDescriptor(request.actor(), request.email());
        dispatcher.dispatchAsync(descriptor);
        // Return 202 Accepted to indicate the command has been accepted for processing.
        // The location header can point to a resource to check the command's status.
        return Response.accepted()
            .location(URI.create("/api/journal/" + descriptor.commandId()))
            .build();
    }

}
