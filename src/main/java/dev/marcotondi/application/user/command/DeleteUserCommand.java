package dev.marcotondi.application.user.command;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.application.user.model.DeleteUserDescriptor;
import dev.marcotondi.application.user.model.DeleteUserPayloadV1;
import dev.marcotondi.application.user.repository.UserRepository;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.exception.CommandDescriptorException;
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
        var email = ((DeleteUserDescriptor) getDescriptor()).payload().email();
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
    public void descriptorFromJournal(
            CommandTypeName type,
            String commandId,
            int payloadVersion,
            String actor,
            String payload,
            LocalDateTime startTime,
            ObjectMapper mapper
    ){

        if (payloadVersion != DeleteUserPayloadV1.version) {
            throw new IllegalStateException("Unsupported payload version for DeleteUser: " + payloadVersion);
        }
        DeleteUserPayloadV1 payloadDto;
        try {
            payloadDto = mapper.readValue(payload, DeleteUserPayloadV1.class);
        } catch (JsonProcessingException e) {
            throw new CommandDescriptorException("Invalid DeleteUser payload", type, payload);
        }
        // Use the canonical constructor to preserve original ID and timestamp
        this.setDescriptor(new DeleteUserDescriptor(
                java.util.UUID.fromString(commandId),
                startTime,
                actor,
                payloadDto));
    }

}
