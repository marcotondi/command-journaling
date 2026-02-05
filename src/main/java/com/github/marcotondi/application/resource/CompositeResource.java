package com.github.marcotondi.application.resource;

import java.net.URI;
import java.util.Map;

import org.jboss.logging.Logger;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.application.composite.SimpleCompositeDescriptor;
import com.github.marcotondi.application.sleep.model.SleepDescriptor;
import com.github.marcotondi.application.user.model.CreateUserDescriptor;
import com.github.marcotondi.core.UtilityCommand;
import com.github.marcotondi.core.api.ICommand;
import com.github.marcotondi.core.api.ICommandFactory;
import com.github.marcotondi.core.api.ICommandManager;
import com.github.marcotondi.core.domain.CommandDescriptor;
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
    @Path("/simple")
    public Response executeCompositeCommand() {
        LOG.info("Start composite command ");

        ICommand<?> composite = commandFactory
                .buildGroupCommand(new SimpleCompositeDescriptor(),
                        new CommandDescriptor[] {
                                new SleepDescriptor(UtilityCommand.SYSTEM_ACTOR, 5),
                                new CreateUserDescriptor(UtilityCommand.SYSTEM_ACTOR, "marco", "marco@email.dev") });

        manager.dispatchAsync(composite);

        return Response.accepted()
                .location(URI.create("/api/journal/" + composite.getDescriptor().getCommandId()))
                .build();
    }

    @POST
    @Path("/create-user")
    public Response createUserCompositeCommand() {
        LOG.info("create-user composite command ");

        Map<String, Object> map = Map.of("actor", UtilityCommand.SYSTEM_ACTOR, "seconds", 10, "username", "marco", "email", "marco@email.dev");

        ICommand<?> composite = commandFactory
                .buildCommand(
                    CommandName.CREATE_USER_COMPOSITE, map);

        manager.dispatchAsync(composite);

        return Response.accepted()
                .location(URI.create("/api/journal/" + composite.getDescriptor().getCommandId()))
                .build();
    }
}
