package dev.marcotondi.application;

import org.jboss.logging.Logger;

import dev.marcotondi.domain.handler.CommandHandler;
import dev.marcotondi.infra.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SleepCommandHandler implements CommandHandler<String, SleepCommand> {

    private static final Logger LOG = Logger.getLogger(CreateUserCommandHandler.class);

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public String handle(SleepCommand command) {
        LOG.infof("Executing SleepCommand for second: %s", command.seconds());

        try {
            Thread.sleep(command.seconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Sleep was interrupted", e);
            return "Sleep was interrupted.";
        }

        return "Sleep of " + command.seconds() + " seconds completed.";
    }

    @Override
    public Class<SleepCommand> getCommandType() {
        return SleepCommand.class;
    }
}
