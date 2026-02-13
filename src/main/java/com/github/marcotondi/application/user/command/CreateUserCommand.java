package com.github.marcotondi.application.user.command;

import static com.github.marcotondi.core.PayloadUtils.extractOrDefaultActor;
import static com.github.marcotondi.core.PayloadUtils.extractOrGenerateTimestamp;
import static com.github.marcotondi.core.PayloadUtils.extractOrGenerateUuid;

import java.util.Map;

import org.jboss.logging.Logger;
import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.application.user.model.CreateUserDescriptor;
import com.github.marcotondi.application.user.model.User;
import com.github.marcotondi.application.user.repository.UserRepository;
import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.domain.Command;
import com.github.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
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
        String email = ((CreateUserDescriptor) getDescriptor()).getEmail();
        LOG.infof("Undoing CreateUserCommand for email: %s", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    userRepository.delete(user);
                    return "User '" + email + "' removed during undo.";
                })
                .orElse("User '" + email + "' not found during undo.");
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {
        var descriptor = new CreateUserDescriptor(
                extractOrGenerateUuid(payload),
                extractOrGenerateTimestamp(payload),
                CommandName.CREATE_USER,
                extractOrDefaultActor(payload),
                (String) payload.get("username"),
                (String) payload.get("email"));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
