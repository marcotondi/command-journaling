package dev.marcotondi.application.model;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SleepCommand implements Command<String, SleepDescriptor> {

    private static final Logger LOG = Logger.getLogger(SleepCommand.class);

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public String execute(SleepDescriptor descriptor) {
        LOG.infof("Executing SleepCommand for second: %s", descriptor.seconds());

        try {
            Thread.sleep(descriptor.seconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Sleep was interrupted", e);
            return "Sleep was interrupted.";
        }

        return "Sleep of " + descriptor.seconds() + " seconds completed.";
    }

    @Override
    public String undo(SleepDescriptor descriptor) {
        LOG.infof("SleepCommand cannot be undone.");
        return "SleepCommand cannot be undone.";
    }

    @Override
    public Class<SleepDescriptor> getCommandType() {
        return SleepDescriptor.class;
    }

}
