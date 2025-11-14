package dev.marcotondi.core.service;

import org.jboss.logging.Logger;

import dev.marcotondi.core.domain.model.CommandExecutedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class CommandAuditLogger {
    private static final Logger LOG = Logger.getLogger(CommandAuditLogger.class);

    public void onCommandExecuted(@ObservesAsync CommandExecutedEvent event) {
        LOG.infof("Command executed: %s [%s]", event.getCommandType(), event.getCommandId());
    }
}
