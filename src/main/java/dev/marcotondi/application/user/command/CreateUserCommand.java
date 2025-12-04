package dev.marcotondi.application.user.command;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;
import dev.marcotondi.application.CommandName;
import dev.marcotondi.application.user.model.CreateUserDescriptor;
import dev.marcotondi.application.user.model.User;
import dev.marcotondi.application.user.repository.UserRepository;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CommandType(CommandName.CREATE_USER)
public class CreateUserCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(CreateUserCommand.class);

    @Inject
    private UserRepository userRepository;

    @Override
    public String doExecute() {
        var descriptor = (CreateUserDescriptor) getDescriptor();

        String username = descriptor.getUsername();
        String email = descriptor.getEmail();

        // Idempotency Check: See if a user with this email already exists.
        if (userRepository.findByEmail(email).isPresent()) {
            LOG.warnf("Attempted to create a user with an existing email: %s. Skipping.", email);
            return "User with email '" + email + "' already exists.";
        }

        LOG.infof("Executing CreateUserCommand for user: %s", username);

        var user = new User(username, email);
        userRepository.persist(user);

        String result = "User '" + username + "' created successfully with ID: " + user.id;
        return result;
    }

    @Override
    public String doUndo() {
        LOG.info("Undo operation is not supported for CreateUserDescriptor.");
        return "Undo operation is not supported for CreateUserDescriptor.";
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {
        var descriptor = new CreateUserDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                LocalDateTime.parse((String) payload.get("timestamp")),
                CommandName.CREATE_USER,
                (String) payload.get("actor"),
                (String) payload.get("username"),
                (String) payload.get("email"));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
