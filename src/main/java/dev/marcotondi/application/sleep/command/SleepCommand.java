package dev.marcotondi.application.sleep.command;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import dev.marcotondi.application.sleep.model.SleepDescriptor;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CommandType("SLEEP")
public class SleepCommand extends Command<String> {
    private static final Logger LOG = Logger.getLogger(SleepCommand.class);

    @Override
    public String doExecute() {
        LOG.infof("Executing SleepCommand for second: %s", ((SleepDescriptor) getDescriptor()).getSeconds());

        try {
            Thread.sleep(((SleepDescriptor) getDescriptor()).getSeconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Sleep was interrupted", e);
            return "Sleep was interrupted.";
        }

        return "Sleep of " + ((SleepDescriptor) getDescriptor()).getSeconds() + " seconds completed.";
    }

    @Override
    public String doUndo() {
        LOG.infof("SleepCommand cannot be undone.");
        return "SleepCommand cannot be undone.";
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {
        var descriptor = new SleepDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                LocalDateTime.parse((String) payload.get("timestamp")),
                CommandTypeName.DELETE_USER,
                (String) payload.get("actor"),
                (Integer) payload.get("seconds"));

        this.setDescriptor(descriptor);
        return descriptor;
    }
}
