package com.github.marcotondi.core;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UtilityCommand {

    public static final String COMMAND_ID = "commandId";
    public static final String TIMESTAMP = "timestamp";
    public static final String COMMAND_TYPE = "commandType";
    public static final String ACTOR = "actor";
    public static final String STATUS = "status";
    public static final String SYSTEM_ACTOR = "system";

    public static UUID extractOrGenerateUuid(Map<String, Object> payload) {
        return Optional.ofNullable(
                    (String) payload.get(COMMAND_ID))
                .map(UUID::fromString)
                .orElseGet(UUID::randomUUID);
    }

    public static LocalDateTime extractOrGenerateTimestamp(Map<String, Object> payload) {
        return Optional.ofNullable(
                    (String) payload.get(TIMESTAMP))
                .map(LocalDateTime::parse)
                .orElseGet(LocalDateTime::now);
    }

    public static String extractOrDefaultActor(Map<String, Object> payload) {
        return Optional.ofNullable(
                    (String) payload.get(ACTOR))
                .orElse(SYSTEM_ACTOR);
    }

}
