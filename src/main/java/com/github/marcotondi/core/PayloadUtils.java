package com.github.marcotondi.core;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PayloadUtils {
    private PayloadUtils() {
    }

    public static UUID extractOrGenerateUuid(Map<String, Object> payload) {
        return Optional.ofNullable(
                (String) payload.get(CoreKeys.COMMAND_ID))
                .map(UUID::fromString)
                .orElseGet(UUID::randomUUID);
    }

    public static LocalDateTime extractOrGenerateTimestamp(Map<String, Object> payload) {
        return Optional.ofNullable(
                (String) payload.get(CoreKeys.TIMESTAMP))
                .map(LocalDateTime::parse)
                .orElseGet(LocalDateTime::now);
    }

    public static String extractOrDefaultActor(Map<String, Object> payload) {
        return Optional.ofNullable(
                (String) payload.get(CoreKeys.ACTOR))
                .orElse(CoreKeys.SYSTEM_ACTOR);
    }

}
