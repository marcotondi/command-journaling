package dev.marcotondi.application.user.command;

import org.jboss.logging.Logger;

import dev.marcotondi.application.user.infra.repository.UserRepository;
import dev.marcotondi.application.user.model.CreateUserDescriptor;
import dev.marcotondi.application.user.model.User;
import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.Initializable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@CommandType("CREATE_USER")
@ApplicationScoped
public class CreateUserCommand implements Command<String>, Initializable<CreateUserDescriptor> {
    private static final Logger LOG = Logger.getLogger(CreateUserCommand.class);

    @Inject
    private UserRepository userRepository;

    private CreateUserDescriptor descriptor;

    @Override
    public void init(CreateUserDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public CreateUserDescriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    @Transactional
    public String execute() {
        var username = descriptor.payload().username();
        var email = descriptor.payload().email();

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
    public String undo() {
        LOG.info("Undo operation is not supported for CreateUserDescriptor.");
        return "Undo operation is not supported for CreateUserDescriptor.";
    }

}
