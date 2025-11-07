package dev.marcotondi.application;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.handler.CommandHandler;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DeleteUserCommandHandler implements CommandHandler<String, DeleteUserCommand> {

    private static final Logger LOG = Logger.getLogger(DeleteUserCommandHandler.class);

    @Inject
    UserRepository userRepository;

    @Override
    public String handle(DeleteUserCommand command) {
        LOG.infof("Executing DeleteUserCommand for email: %s", command.email());
        
        return userRepository.findByEmail(command.email())
            .map(user -> {
                userRepository.delete(user);
                return "User '" + command.email() + "' deleted successfully with ID: " + user.id;
            })
            .orElse("User with email '" + command.email() + "' not found.");
    }

    @Override
    public Class<DeleteUserCommand> getCommandType() {
        return DeleteUserCommand.class;
    }

}
