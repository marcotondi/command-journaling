package dev.marcotondi.application.user.command;

import org.jboss.logging.Logger;

import dev.marcotondi.application.user.infra.repository.UserRepository;
import dev.marcotondi.application.user.model.DeleteUserDescriptor;
import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.Initializable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@CommandType("DELETE_USER")
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
        var email = descriptor.payload().email();
        LOG.infof("Executing DeleteUserCommand for email: %s", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    userRepository.delete(user);
                    return "User '" + email + "' deleted successfully with ID: " + user.id;
                })
                .orElse("User with email '" + email + "' not found.");
    }

    @Override
    public String undo() {
        LOG.info("Undo operation is not supported for DeleteUserCommand.");
        return "Undo operation is not supported for DeleteUserCommand.";
    }

}
