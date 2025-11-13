package dev.marcotondi.user.recovery;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.domain.CommandReconstructor;
import dev.marcotondi.core.domain.CommandTypeName;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.user.model.CreateUserDescriptor;
import dev.marcotondi.user.model.CreateUserPayloadV1;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateUserReconstructor implements CommandReconstructor {

    @Override
    public CommandTypeName supportedType() {
        return CommandTypeName.CREATE_USER;
    }

    @Override
    public CommandDescriptor reconstruct(JournalEntry entry, ObjectMapper objectMapper) throws IOException {
        if (entry.payloadVersion != 1) {
            throw new IllegalStateException("Unsupported payload version for CreateUser: " + entry.payloadVersion);
        }
        CreateUserPayloadV1 payloadDto = objectMapper.readValue(entry.payload, CreateUserPayloadV1.class);
        // Use the canonical constructor to preserve original ID and timestamp
        return new CreateUserDescriptor(
            java.util.UUID.fromString(entry.commandId),
            entry.startTime,
            entry.actor,
            payloadDto
        );
    }
}
