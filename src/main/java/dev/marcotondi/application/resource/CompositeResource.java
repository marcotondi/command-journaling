package dev.marcotondi.application.resource;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import dev.marcotondi.application.exception.CompositeCommandCreationException;
import dev.marcotondi.application.model.CompositeCommandDescriptor;
import dev.marcotondi.application.model.CreateUserDescriptor;
import dev.marcotondi.application.model.DeleteUserDescriptor;
import dev.marcotondi.application.model.SleepDescriptor;
import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CompositeCommand;
import dev.marcotondi.infra.CommandFactory;
import dev.marcotondi.infra.CommandManager;
import jakarta.enterprise.inject.Instance;
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
    CommandManager manager;

    @Inject
    CommandFactory commandFactory;

    @Inject
    Instance<CompositeCommand> compositeCommandInstance;

    record CommandSpec(String type, Map<String, Object> payload) { }
    record CompositeCommandRequest(String actor, List<CommandSpec> commands) { }

    @POST
    public Response executeCompositeCommand(CompositeCommandRequest request) {
        LOG.infof("Received composite command from actor: %s with %d commands",
                request.actor, request.commands.size());

        CompositeCommand compositeCommand = buildCompositeCommand(request);
        CompositeCommandDescriptor descriptor = compositeCommand.getDescriptor();

        manager.dispatchAsync(compositeCommand);

        return Response.accepted()
                .location(URI.create("/api/journal/" + descriptor.commandId()))
                .build();
    }

    private CompositeCommand buildCompositeCommand(CompositeCommandRequest request) {
        CompositeCommand compositeCommand = compositeCommandInstance.get();
        List<CommandDescriptor> childDescriptors = new ArrayList<>();

        for (int i = 0; i < request.commands.size(); i++) {
            CommandSpec spec = request.commands.get(i);

            try {
                CommandDescriptor descriptor = createDescriptorFromSpec(spec, request.actor, i);
                childDescriptors.add(descriptor);

                Command<?> command = commandFactory.buildCommand(descriptor);
                compositeCommand.addCommand(command);

            } catch (IllegalStateException e) {
                throw new CompositeCommandCreationException(
                        "Command type '" + spec.type + "' not found",
                        spec.type, spec.payload, e);
            }
        }

        CompositeCommandDescriptor compositeDescriptor = new CompositeCommandDescriptor(
                UUID.randomUUID(),
                LocalDateTime.now(),
                request.actor,
                "CompositeCommand",
                childDescriptors);

        compositeCommand.withDescriptor(compositeDescriptor);
        return compositeCommand;
    }

    private CommandDescriptor createDescriptorFromSpec(
        CommandSpec spec,
        String actor,
        int index
    ) {

        return switch (spec.type) {
            case "CreateUser" -> new CreateUserDescriptor(
                            actor,
                            (String) spec.payload.get("username"),
                            (String) spec.payload.get("email"));

            case "DeleteUser" -> new DeleteUserDescriptor(
                            actor,
                            (String) spec.payload.get("email"));
            case "Sleep" -> new SleepDescriptor(
                            actor,
                            (int) spec.payload.get("seconds"));

            default -> throw new CompositeCommandCreationException(
                            "Unknown command type: " + spec.type,
                            spec.type,
                            spec.payload);
        };
    }

}
