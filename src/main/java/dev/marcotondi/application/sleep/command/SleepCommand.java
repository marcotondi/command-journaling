package dev.marcotondi.application.sleep.command;

import org.jboss.logging.Logger;

import dev.marcotondi.application.sleep.model.SleepDescriptor;
import dev.marcotondi.core.api.Command;
import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.Initializable;
import jakarta.enterprise.context.ApplicationScoped;

@CommandType("SLEEP")
@ApplicationScoped
public class SleepCommand implements Command<String>, Initializable<SleepDescriptor> {

    private static final Logger LOG = Logger.getLogger(SleepCommand.class);

    private SleepDescriptor descriptor;

    @Override
    public void init(SleepDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public CommandDescriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    public String execute() {
        LOG.infof("Executing SleepCommand for second: %s", descriptor.payload().seconds());

        try {
            Thread.sleep(descriptor.payload().seconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Sleep was interrupted", e);
            return "Sleep was interrupted.";
        }

        return "Sleep of " + descriptor.payload().seconds() + " seconds completed.";
    }

    @Override
    public String undo() {
        LOG.infof("SleepCommand cannot be undone.");
        return "SleepCommand cannot be undone.";
    }

}
