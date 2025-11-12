package dev.marcotondi.application;

import java.util.Map;

public record CommandRequest(String type, Map<String, Object> payload) {}
