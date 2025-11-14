package dev.marcotondi.core.payload;

import dev.marcotondi.core.api.CommandDescriptor;
import dev.marcotondi.core.api.Payload;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PayloadMapper {

    public Payload toPayload(CommandDescriptor descriptor) {
        // Assuming CommandDescriptor has a getPayload() method
        return descriptor.getPayload();
    }
}
