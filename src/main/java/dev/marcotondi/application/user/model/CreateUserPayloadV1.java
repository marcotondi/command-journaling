package dev.marcotondi.application.user.model;

import dev.marcotondi.core.api.Payload;

public record CreateUserPayloadV1(String username, String email) implements Payload {}
