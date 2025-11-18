package dev.marcotondi.application.sleep.command;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.application.sleep.model.SleepDescriptor;
import dev.marcotondi.application.sleep.model.SleepPayloadV1;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.exception.CommandDescriptorException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CommandType("SLEEP")
public class SleepCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(SleepCommand.class);

    @Override
    public String doExecute() {
        LOG.infof("Executing SleepCommand for second: %s", ((SleepDescriptor) getDescriptor()).payload().seconds());

        try {
            Thread.sleep(((SleepDescriptor) getDescriptor()).payload().seconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Sleep was interrupted", e);
            return "Sleep was interrupted.";
        }

        return "Sleep of " + ((SleepDescriptor) getDescriptor()).payload().seconds() + " seconds completed.";
    }

    @Override
    public String doUndo() {
        LOG.infof("SleepCommand cannot be undone.");
        return "SleepCommand cannot be undone.";
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

        if (payloadVersion != SleepPayloadV1.version) {
            throw new CommandDescriptorException("Unsupported payload version for Sleep: " + payloadVersion, type, payload);
        }
        SleepPayloadV1 payloadDto;
        try {
            payloadDto = mapper.readValue(payload, SleepPayloadV1.class);
        } catch (JsonProcessingException e) {
            throw new CommandDescriptorException("Invalid Sleep payload", type, payload);
        }
        // Use the canonical constructor to preserve original ID and timestamp
        this.setDescriptor(new SleepDescriptor(
                java.util.UUID.fromString(commandId),
                startTime,
                actor,
                payloadDto));
    }

}
