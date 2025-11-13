package dev.marcotondi.user.recovery;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.core.domain.CommandDescriptor;
import dev.marcotondi.core.domain.CommandReconstructor;
import dev.marcotondi.core.domain.CommandTypeName;
import dev.marcotondi.journal.domain.JournalEntry;
import dev.marcotondi.user.model.DeleteUserDescriptor;
import dev.marcotondi.user.model.DeleteUserPayloadV1;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeleteUserReconstructor implements CommandReconstructor {

    @Override
    public CommandTypeName supportedType() {
        return CommandTypeName.DELETE_USER;
    }

    @Override
    public CommandDescriptor reconstruct(JournalEntry entry, ObjectMapper objectMapper) throws IOException {
        if (entry.payloadVersion != 1) {
            throw new IllegalStateException("Unsupported payload version for DeleteUser: " + entry.payloadVersion);
        }
        DeleteUserPayloadV1 payloadDto = objectMapper.readValue(entry.payload, DeleteUserPayloadV1.class);
        // Use the canonical constructor to preserve original ID and timestamp
        return new DeleteUserDescriptor(
            java.util.UUID.fromString(entry.commandId),
            entry.startTime,
            entry.actor,
            payloadDto
        );
    }
}
