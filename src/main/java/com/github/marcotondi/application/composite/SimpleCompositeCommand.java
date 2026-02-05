package com.github.marcotondi.application.composite;

import static com.github.marcotondi.core.UtilityCommand.*;

import java.util.Map;

import com.github.marcotondi.application.CommandName;
import com.github.marcotondi.core.api.CommandType;
import com.github.marcotondi.core.domain.CommandComposite;
import com.github.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * An example composite command that demonstrates how to use CommandComposite.
 * This command executes a 'sleep' operation followed by creating a 'todo' item.
 */
@ApplicationScoped
@CommandType(CommandName.SIMPLE_COMPOSITE)
public class SimpleCompositeCommand extends CommandComposite<Void> {

    @Override
    public CommandDescriptor setDescriptor(
            Map<String, Object> payload) {

        var descriptor = new SimpleCompositeDescriptor(
                extractOrGenerateUuid(payload),
                extractOrGenerateTimestamp(payload),
                CommandName.SIMPLE_COMPOSITE,
                extractOrDefaultActor(payload));

        this.setDescriptor(descriptor);
        return descriptor;
    }

}
