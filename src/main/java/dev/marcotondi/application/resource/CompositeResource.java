package dev.marcotondi.application.resource;

import java.net.URI;
import org.jboss.logging.Logger;
import dev.marcotondi.application.composite.SimpleCompositeDescriptor;
import dev.marcotondi.application.sleep.model.SleepDescriptor;
import dev.marcotondi.application.user.model.CreateUserDescriptor;
import dev.marcotondi.core.api.ICommand;
import dev.marcotondi.core.api.ICommandFactory;
import dev.marcotondi.core.api.ICommandManager;
import dev.marcotondi.core.domain.CommandDescriptor;
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

        ICommand<?> composite = commandFactory
                .buildCommand(new SimpleCompositeDescriptor(
                        "SIMPLE_COMPOSITE",
                        "system",
                        new CommandDescriptor[] {
                                new SleepDescriptor("system", 5),
                                new CreateUserDescriptor("system", "marco", "marco@email.dev")}));

        manager.dispatchAsync(composite);

        return Response.accepted()
                .location(URI.create("/api/journal/" + composite.getDescriptor().getCommandId()))
                .build();
    }

}
