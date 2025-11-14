package dev.marcotondi.core.service;

import dev.marcotondi.core.domain.model.CommandExecutedEvent;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class CommandMetricsCollectorOTel {

    private final LongCounter commandExecutedCounter;

    public CommandMetricsCollectorOTel() {
        Meter meter = GlobalOpenTelemetry.getMeter("quarkus-command-journaling");
        this.commandExecutedCounter = meter
                .counterBuilder("commands_executed")
                .setDescription("Number of commands executed successfully")
                .setUnit("1")
                .build();
    }

    public void onCommandExecuted(@ObservesAsync CommandExecutedEvent event) {
        commandExecutedCounter.add(1, Attributes.builder()
                .put("type", event.getCommandType())
                .build());
    }
}
