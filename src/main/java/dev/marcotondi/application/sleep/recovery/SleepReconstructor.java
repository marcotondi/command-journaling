package dev.marcotondi.application.sleep.recovery;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.application.sleep.model.SleepDescriptor;
import dev.marcotondi.application.sleep.model.SleepPayloadV1;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandReconstructor;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.journal.domain.JournalEntry;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SleepReconstructor implements CommandReconstructor {

    @Override
    public CommandTypeName supportedType() {
        return CommandTypeName.SLEEP;
    }

    @Override
    public CommandDescriptor reconstruct(JournalEntry entry, ObjectMapper objectMapper) throws IOException {
        if (entry.payloadVersion != 1) {
            throw new IllegalStateException("Unsupported payload version for Sleep: " + entry.payloadVersion);
        }
        SleepPayloadV1 payloadDto = objectMapper.readValue(entry.payload, SleepPayloadV1.class);
        // Use the canonical constructor to preserve original ID and timestamp
        return new SleepDescriptor(
            java.util.UUID.fromString(entry.commandId),
            entry.startTime,
            entry.actor,
            payloadDto
        );
    }
}
