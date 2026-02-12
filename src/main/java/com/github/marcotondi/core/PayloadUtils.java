package com.github.marcotondi.core;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.Arc;

public class PayloadUtils {
    private PayloadUtils() {
    }

    private static ObjectMapper mapper() {
        return Arc.container().instance(ObjectMapper.class).get();
    }

    public static <T> T convertValue(Object value, Class<T> targetClass) {
        return mapper().convertValue(value, targetClass);
    }

    public static <T> List<T> convertList(List<?> list, Class<T> targetClass) {
        if (list == null)
            return Collections.emptyList();
        return list.stream()
                .map(item -> mapper().convertValue(item, targetClass))
                .collect(Collectors.toList());
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
