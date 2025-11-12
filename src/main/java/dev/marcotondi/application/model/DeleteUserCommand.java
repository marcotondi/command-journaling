package dev.marcotondi.application.model;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DeleteUserCommand implements Command<String, DeleteUserDescriptor> {

    private static final Logger LOG = Logger.getLogger(DeleteUserCommand.class);

    @Inject
    UserRepository userRepository;

    @Override
    public String execute(DeleteUserDescriptor descriptor) {
        LOG.infof("Executing DeleteUserCommand for email: %s", descriptor.email());

        return userRepository.findByEmail(descriptor.email())
            .map(user -> {
                userRepository.delete(user);
                return "User '" + descriptor.email() + "' deleted successfully with ID: " + user.id;
            })
            .orElse("User with email '" + descriptor.email() + "' not found.");
    }

    @Override
    public String undo(DeleteUserDescriptor command) {
        LOG.info("Undo operation is not supported for DeleteUserCommand.");
        return "Undo operation is not supported for DeleteUserCommand.";
    }

    @Override
    public Class<DeleteUserDescriptor> getCommandType() {
        return DeleteUserDescriptor.class;
    }

}
