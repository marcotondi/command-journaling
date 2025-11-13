package dev.marcotondi.user.model;

import dev.marcotondi.core.domain.Payload;

public record CreateUserPayloadV1(String username, String email) implements Payload {}
