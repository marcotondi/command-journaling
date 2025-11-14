package dev.marcotondi.application.user.model;

import dev.marcotondi.core.api.Payload;

public record DeleteUserPayloadV1(String email) implements Payload {}
