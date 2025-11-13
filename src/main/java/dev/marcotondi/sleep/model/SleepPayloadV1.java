package dev.marcotondi.sleep.model;

import dev.marcotondi.core.domain.Payload;

public record SleepPayloadV1(int seconds) implements Payload {}
