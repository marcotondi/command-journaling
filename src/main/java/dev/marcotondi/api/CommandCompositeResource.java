package dev.marcotondi.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.marcotondi.application.CreateUserCommand;
import dev.marcotondi.application.DeleteUserCommand;
import dev.marcotondi.application.SleepCommand;
import dev.marcotondi.domain.model.Command;
import dev.marcotondi.domain.model.CompositeCommand;
import dev.marcotondi.infra.CommandDispatcher;
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
    CommandDispatcher dispatcher;

    // Records for composite command request
    public record CommandRequest(String type, Map<String, Object> payload) {}
    public record CompositeCommandRequest(String actor, List<CommandRequest> commands) {}

    @POST
    public Response executeCompositeCommand(@Valid CompositeCommandRequest request) {
        var commands = new ArrayList<Command<?>>();

        for (CommandRequest cmdRequest : request.commands()) {
            commands.add(createCommand(cmdRequest, request.actor()));
        }

        var compositeCommand = new CompositeCommand(
            java.util.UUID.randomUUID(),
            java.time.LocalDateTime.now(),
            request.actor(),
            commands
        );

        dispatcher.dispatch(compositeCommand);

        return Response.accepted()
            .location(URI.create("/api/journal/" + compositeCommand.commandId()))
            .build();
    }

    private Command<?> createCommand(CommandRequest request, String actor) {

        return switch (request.type()) {
            case "CreateUser" -> new CreateUserCommand(
                actor,
                (String) request.payload().get("username"),
                (String) request.payload().get("email")
            );
            case "DeleteUser" -> new DeleteUserCommand(
                actor,
                (String) request.payload().get("email")
            );
            case "Sleep" -> new SleepCommand(
                actor,
                (Integer) request.payload().get("seconds")
            );
            default -> throw new IllegalArgumentException("Unknown command type: " + request.type());
        };
    }
}
