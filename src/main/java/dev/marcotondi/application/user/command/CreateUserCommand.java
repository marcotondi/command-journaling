package dev.marcotondi.application.user.command;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.application.user.model.CreateUserDescriptor;
import dev.marcotondi.application.user.model.CreateUserPayloadV1;
import dev.marcotondi.application.user.model.User;
import dev.marcotondi.application.user.repository.UserRepository;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.exception.CommandDescriptorException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CommandType("CREATE_USER")
public class CreateUserCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(CreateUserCommand.class);

    @Inject
    private UserRepository userRepository;

    @Override
    public String doExecute() {
        String username = ((CreateUserPayloadV1) ((CreateUserDescriptor) getDescriptor()).getPayload()).username();
        String email = ((CreateUserPayloadV1) ((CreateUserDescriptor) getDescriptor()).getPayload()).email();

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
    public String doUndo() {
        LOG.info("Undo operation is not supported for CreateUserDescriptor.");
        return "Undo operation is not supported for CreateUserDescriptor.";
    }

    @Override
    public void descriptorFromJournal(
            CommandTypeName type,
            String commandId,
            int payloadVersion,
            String actor,
            String payload,
            LocalDateTime startTime,
            ObjectMapper mapper) {

        if (payloadVersion != CreateUserPayloadV1.version) {
            throw new IllegalStateException("Unsupported payload version for CreateUser: " + payloadVersion);
        }
        CreateUserPayloadV1 payloadDto;
        try {
            payloadDto = mapper.readValue(payload, CreateUserPayloadV1.class);
        } catch (JsonProcessingException e) {
            throw new CommandDescriptorException("Invalid CreateUser payload", type, payload);
        }

        // Use the canonical constructor to preserve original ID and timestamp
        this.setDescriptor(new CreateUserDescriptor(
                java.util.UUID.fromString(commandId),
                startTime,
                actor,
                payloadDto));
    }

}
