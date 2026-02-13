package com.github.marcotondi.application.user.command;

import static com.github.marcotondi.core.PayloadUtils.extractOrDefaultActor;
import static com.github.marcotondi.core.PayloadUtils.extractOrGenerateTimestamp;
import static com.github.marcotondi.core.PayloadUtils.extractOrGenerateUuid;

import java.util.Map;

import org.jboss.logging.Logger;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.application.user.model.DeleteUserDescriptor;
import com.github.marcotondi.application.user.model.User;
import com.github.marcotondi.application.user.repository.UserRepository;
import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.domain.Command;
import com.github.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@CommandType(CommandName.DELETE_USER)
public class DeleteUserCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(DeleteUserCommand.class);

    @Inject
    private UserRepository userRepository;

    private User deletedUser;

    @Override
    public String doExecute() {
        var descriptor = (DeleteUserDescriptor) getDescriptor();

        var email = descriptor.getEmail();
        LOG.infof("Executing DeleteUserCommand for email: %s", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    this.deletedUser = user;
                    userRepository.delete(user);
                    return "User '" + email + "' deleted successfully with ID: " + user.id;
                })
                .orElse("User with email '" + email + "' not found.");
    }

    @Override
    public String doUndo() {
        if (deletedUser == null) {
            LOG.info("Undo DeleteUserCommand: nothing to restore.");
            return "Nothing to restore for DeleteUserCommand.";
        }

        userRepository.persist(deletedUser);
        LOG.infof("Restored user '%s' during undo.", deletedUser.getEmail());
        return "User '" + deletedUser.getEmail() + "' restored during undo.";
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {
        var descriptor = new DeleteUserDescriptor(
                extractOrGenerateUuid(payload),
                extractOrGenerateTimestamp(payload),
                CommandName.DELETE_USER,
                extractOrDefaultActor(payload),
                (String) payload.get("email"));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
