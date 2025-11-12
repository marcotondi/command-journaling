package dev.marcotondi.domain.api;

import java.util.List;

public interface CompositeCommandDescriptor<R> extends CommandDescriptor<R> {

    List<CommandDescriptor<?>> commands();
}
