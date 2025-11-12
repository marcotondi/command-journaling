package dev.marcotondi.application;

import java.util.List;
import java.util.Map;

import dev.marcotondi.domain.api.CommandDescriptor;
import dev.marcotondi.domain.api.CompositeCommandDescriptor;

public interface CommandDescriptorFactory {
    CommandDescriptor<?> create(String commandType, String actor, Map<String, Object> payload);

    CompositeCommandDescriptor<?> createComposite(String actor, List<CommandRequest> commands);
}
