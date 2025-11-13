package dev.marcotondi.user.model;

import dev.marcotondi.core.domain.Payload;

public record DeleteUserPayloadV1(String email) implements Payload {}
