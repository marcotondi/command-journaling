package dev.marcotondi.application.model;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.api.CommandType;
import dev.marcotondi.domain.api.Initializable;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@CommandType("DeleteUser")
@ApplicationScoped
public class DeleteUserCommand implements Command<String>, Initializable<DeleteUserDescriptor> {
    private static final Logger LOG = Logger.getLogger(DeleteUserCommand.class);

    @Inject
    private UserRepository userRepository;

    private DeleteUserDescriptor descriptor;

    @Override
    public void init(DeleteUserDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public DeleteUserDescriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    @Transactional
    public String execute() {
        LOG.infof("Executing DeleteUserCommand for email: %s", descriptor.email());

        return userRepository.findByEmail(descriptor.email())
                .map(user -> {
                    userRepository.delete(user);
                    return "User '" + descriptor.email() + "' deleted successfully with ID: " + user.id;
                })
                .orElse("User with email '" + descriptor.email() + "' not found.");
    }

    @Override
    public String undo() {
        LOG.info("Undo operation is not supported for DeleteUserCommand.");
        return "Undo operation is not supported for DeleteUserCommand.";
    }

}
