package dev.marcotondi.application.resource;

import java.net.URI;

import dev.marcotondi.application.user.model.CreateUserDescriptor;
import dev.marcotondi.application.user.model.DeleteUserDescriptor;
import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.infra.CommandFactory;
import dev.marcotondi.core.infra.CommandManager;
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
    CommandManager manager;

    @Inject
    CommandFactory commandFactory;

    // Record for the request payload
    public record CreateUserRequest(String username, String email, String actor) {}

    @POST
    @Path("/users/create")
    public Response createUser(@Valid CreateUserRequest request) {
        var descriptor = new CreateUserDescriptor(request.actor(), request.username(), request.email());

        // Create the Command object using the factory
        // Assuming CreateUserCommand implements Command<User> and Initializable<CreateUserDescriptor>
        Command<?> createUserCommand = commandFactory.buildCommand(descriptor);

        manager.dispatch(createUserCommand);
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

        // Create the Command object using the factory
        // Assuming DeleteUserCommand implements Command<Void> and Initializable<DeleteUserDescriptor>
        Command<?> deleteUserCommand = commandFactory.buildCommand(descriptor);

        manager.dispatchAsync(deleteUserCommand);
        // Return 202 Accepted to indicate the command has been accepted for processing.
        // The location header can point to a resource to check the command's status.
        return Response.accepted()
            .location(URI.create("/api/journal/" + descriptor.commandId()))
            .build();
    }

}
