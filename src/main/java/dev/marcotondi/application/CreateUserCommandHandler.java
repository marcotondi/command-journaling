package dev.marcotondi.application;

import org.jboss.logging.Logger;

import dev.marcotondi.application.model.User;
import dev.marcotondi.domain.handler.CommandHandler;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateUserCommandHandler implements CommandHandler<String, CreateUserCommand> {

    private static final Logger LOG = Logger.getLogger(CreateUserCommandHandler.class);

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public String handle(CreateUserCommand command) {
        // Idempotency Check: See if a user with this email already exists.
        if (userRepository.findByEmail(command.email()).isPresent()) {
            LOG.warnf("Attempted to create a user with an existing email: %s. Skipping.", command.email());
            return "User with email '" + command.email() + "' already exists.";
        }

        LOG.infof("Executing CreateUserCommand for user: %s", command.username());

        var user = new User(command.username(), command.email());
        userRepository.persist(user);

        String result = "User '" + command.username() + "' created successfully with ID: " + user.id;
        return result;
    }

    @Override
    public Class<CreateUserCommand> getCommandType() {
        return CreateUserCommand.class;
    }
}
