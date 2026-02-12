package com.github.marcotondi.core;

import java.util.HashMap;
import java.util.Map;

import com.github.marcotondi.core.entity.JournalEntity;

public record JournalResponse(
        String commandId,
        String commandType,
        Map<String, Object> payload) {
    public static JournalResponse from(JournalEntity journal) {
        var enrichedPayload = new HashMap<String, Object>();

        if (journal.payload != null) {
            enrichedPayload.putAll(journal.payload);
        }

        enrichedPayload.put(CoreKeys.COMMANDS_PAYLOAD_KEY, journal.commands);

        return new JournalResponse(
                journal.commandId,
                journal.commandType,
                enrichedPayload);
    }
}
