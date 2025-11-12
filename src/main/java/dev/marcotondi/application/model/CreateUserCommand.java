package dev.marcotondi.application.model;

import org.jboss.logging.Logger;

import dev.marcotondi.application.entity.User;
import dev.marcotondi.domain.api.Command;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateUserCommand implements Command<String, CreateUserDescriptor> {

    private static final Logger LOG = Logger.getLogger(CreateUserCommand.class);

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public String execute(CreateUserDescriptor descriptor) {
        // Idempotency Check: See if a user with this email already exists.
        if (userRepository.findByEmail(descriptor.email()).isPresent()) {
            LOG.warnf("Attempted to create a user with an existing email: %s. Skipping.", descriptor.email());
            return "User with email '" + descriptor.email() + "' already exists.";
        }

        LOG.infof("Executing CreateUserCommand for user: %s", descriptor.username());

        var user = new User(descriptor.username(), descriptor.email());
        userRepository.persist(user);

        String result = "User '" + descriptor.username() + "' created successfully with ID: " + user.id;
        return result;
    }

    @Override
    public String undo(CreateUserDescriptor command) {
        LOG.info("Undo operation is not supported for CreateUserDescriptor.");
        return "Undo operation is not supported for CreateUserDescriptor.";
    }

    @Override
    public Class<CreateUserDescriptor> getCommandType() {
        return CreateUserDescriptor.class;
    }

}
