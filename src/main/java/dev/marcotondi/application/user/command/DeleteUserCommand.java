package dev.marcotondi.application.user.command;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import dev.marcotondi.application.user.model.DeleteUserDescriptor;
import dev.marcotondi.application.user.repository.UserRepository;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@CommandType("DELETE_USER")
public class DeleteUserCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(DeleteUserCommand.class);

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional
    public String doExecute() {
        var descriptor = (DeleteUserDescriptor) getDescriptor();

        var email = descriptor.getEmail();
        LOG.infof("Executing DeleteUserCommand for email: %s", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    userRepository.delete(user);
                    return "User '" + email + "' deleted successfully with ID: " + user.id;
                })
                .orElse("User with email '" + email + "' not found.");
    }

    @Override
    public String doUndo() {
        LOG.info("Undo operation is not supported for DeleteUserCommand.");
        return "Undo operation is not supported for DeleteUserCommand.";
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {
        var descriptor = new DeleteUserDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                LocalDateTime.parse((String) payload.get("timestamp")),
                CommandTypeName.DELETE_USER,
                (String) payload.get("actor"),
                (String) payload.get("email"));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
