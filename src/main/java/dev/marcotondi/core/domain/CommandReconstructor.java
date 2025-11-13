package dev.marcotondi.core.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcotondi.journal.domain.JournalEntry;

public interface CommandReconstructor {

    CommandTypeName supportedType();

    CommandDescriptor reconstruct(JournalEntry entry, ObjectMapper objectMapper) throws java.io.IOException;

}
