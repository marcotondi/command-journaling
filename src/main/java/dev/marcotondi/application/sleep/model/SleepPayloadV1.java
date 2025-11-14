package dev.marcotondi.application.sleep.model;

import dev.marcotondi.core.api.Payload;

public record SleepPayloadV1(int seconds) implements Payload {}
