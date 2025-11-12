package dev.marcotondi.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.marcotondi.application.model.CreateUserDescriptor;
import dev.marcotondi.application.model.DefaultCompositeCommandDescriptor;
import dev.marcotondi.application.model.DeleteUserDescriptor;
import dev.marcotondi.application.model.SleepDescriptor;
import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CompositeCommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultCommandDescriptorFactory implements CommandDescriptorFactory {

    @Override
    public CommandDescriptor<?> create(String commandType, String actor, Map<String, Object> payload) {
        return switch (commandType) {
            case "CreateUser" -> new CreateUserDescriptor(
                actor,
                (String) payload.get("username"),
                (String) payload.get("email")
            );
            case "DeleteUser" -> new DeleteUserDescriptor(
                actor,
                (String) payload.get("email")
            );
            case "Sleep" -> new SleepDescriptor(
                actor,
                ((Number) payload.get("seconds")).intValue()
            );
            default -> throw new IllegalArgumentException("Unknown command type: " + commandType);
        };
    }

    @Override
    public CompositeCommandDescriptor<?> createComposite(String actor, List<CommandRequest> commands) {
        var commandDescriptors = new ArrayList<CommandDescriptor<?>>();
        for (CommandRequest cmdRequest : commands) {
            commandDescriptors.add(create(cmdRequest.type(), actor, cmdRequest.payload()));
        }

        return new DefaultCompositeCommandDescriptor(
            UUID.randomUUID(),
            LocalDateTime.now(),
            actor,
            commandDescriptors
        );
    }
}
