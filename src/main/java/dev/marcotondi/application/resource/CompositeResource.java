// package dev.marcotondi.application.resource;

// import java.net.URI;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.jboss.logging.Logger;

// import dev.marcotondi.composite.model.exception.CompositeCommandCreationException;
// import dev.marcotondi.composite.model.CompositeCommandDescriptor;
// import dev.marcotondi.application.sleep.model.SleepDescriptor;
// import dev.marcotondi.application.user.model.CreateUserDescriptor;
// import dev.marcotondi.application.user.model.DeleteUserDescriptor;
// import dev.marcotondi.composite.command.CompositeCommand;
// import dev.marcotondi.core.api.Command;
// import dev.marcotondi.core.api.CommandDescriptor;
// import dev.marcotondi.core.service.CommandFactory;
// import dev.marcotondi.core.service.CommandManager;
// import jakarta.enterprise.inject.Instance;
// import jakarta.inject.Inject;
// import jakarta.ws.rs.Consumes;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.Path;
// import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;

// @Path("/api/commands/composite")
// @Produces(MediaType.APPLICATION_JSON)
// @Consumes(MediaType.APPLICATION_JSON)
// public class CompositeResource {
//     private static final Logger LOG = Logger.getLogger(CompositeResource.class);

//     @Inject
//     CommandManager manager;

//     @Inject
//     CommandFactory commandFactory;

//     @Inject
//     Instance<CompositeCommand> compositeCommandInstance;

//     record CommandSpec(String type, Map<String, Object> payload) { }
//     record CompositeCommandRequest(String actor, List<CommandSpec> commands) { }

//     @POST
//     public Response executeCompositeCommand(CompositeCommandRequest request) {
//         LOG.infof("Received composite command from actor: %s with %d commands",
//                 request.actor, request.commands.size());

//         CompositeCommand compositeCommand = buildCompositeCommand(request);
//         CompositeCommandDescriptor descriptor = compositeCommand.getDescriptor();

//         manager.dispatchAsync(compositeCommand);

//         return Response.accepted()
//                 .location(URI.create("/api/journal/" + descriptor.commandId()))
//                 .build();
//     }

//     private CompositeCommand buildCompositeCommand(CompositeCommandRequest request) {
//         CompositeCommand compositeCommand = compositeCommandInstance.get();
//         List<CommandDescriptor> childDescriptors = new ArrayList<>();

//         for (int i = 0; i < request.commands.size(); i++) {
//             CommandSpec spec = request.commands.get(i);

//             try {
//                 CommandDescriptor descriptor = createDescriptorFromSpec(spec, request.actor, i);
//                 childDescriptors.add(descriptor);

//                 Command<?> command = commandFactory.buildCommand(descriptor);
//                 compositeCommand.addCommand(command);

//             } catch (IllegalStateException e) {
//                 throw new CompositeCommandCreationException(
//                         "Command type '" + spec.type + "' not found",
//                         spec.type, spec.payload, e);
//             }
//         }

//         CompositeCommandDescriptor compositeDescriptor = new CompositeCommandDescriptor(
//                 request.actor,
//                 childDescriptors);

//         compositeCommand.withDescriptor(compositeDescriptor);
//         return compositeCommand;
//     }

//     private CommandDescriptor createDescriptorFromSpec(
//         CommandSpec spec,
//         String actor,
//         int index
//     ) {

//         return switch (spec.type) {
//             case "CreateUser" -> new CreateUserDescriptor(
//                             actor,
//                             (String) spec.payload.get("username"),
//                             (String) spec.payload.get("email"));

//             case "DeleteUser" -> new DeleteUserDescriptor(
//                             actor,
//                             (String) spec.payload.get("email"));
//             case "Sleep" -> new SleepDescriptor(
//                             actor,
//                             (int) spec.payload.get("seconds"));

//             default -> throw new CompositeCommandCreationException(
//                             "Unknown command type: " + spec.type,
//                             spec.type,
//                             spec.payload);
//         };
//     }

// }
