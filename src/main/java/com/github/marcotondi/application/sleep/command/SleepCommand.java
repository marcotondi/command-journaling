package com.github.marcotondi.application.sleep.command;

import static com.github.marcotondi.core.UtilityCommand.extractOrDefaultActor;
import static com.github.marcotondi.core.UtilityCommand.extractOrGenerateTimestamp;
import static com.github.marcotondi.core.UtilityCommand.extractOrGenerateUuid;

import java.util.Map;

import org.jboss.logging.Logger;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.application.sleep.model.SleepDescriptor;
import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.domain.Command;
import com.github.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CommandType(CommandName.SLEEP)
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
                extractOrGenerateUuid(payload),
                extractOrGenerateTimestamp(payload),
                CommandName.SLEEP,
                extractOrDefaultActor(payload),
                (Integer) payload.get("seconds"));

        this.setDescriptor(descriptor);
        return descriptor;
    }
}
