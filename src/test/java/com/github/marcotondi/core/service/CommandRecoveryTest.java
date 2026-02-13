package com.github.marcotondi.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.github.marcotondi.application.sleep.model.SleepDescriptor;
import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.JournalService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class CommandRecoveryTest {

    @Inject
    CommandRecoveryService recoveryService;

    @Inject
    JournalService journalService;

    @Test
    void recoversPendingCommand() {
        var descriptor = new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1);
        journalService.createJournalEntity(descriptor, null);

        assertEquals(CommandStatus.PENDING,
                journalService.findByCommandId(descriptor.getCommandId()).orElseThrow().status);

        recoveryService.recoverInterruptedCommands();

        assertEquals(CommandStatus.COMPLETED,
                awaitTerminalStatus(descriptor.getCommandId(), Duration.ofSeconds(15)));
    }

    @Test
    void reclaimsRecoveringCommandWithStaleLease() {
        var descriptor = new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1);
        var entry = journalService.createJournalEntity(descriptor, null);
        entry.status = CommandStatus.RECOVERING;
        entry.recoveryStartedAt = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10);
        entry.update();

        recoveryService.recoverInterruptedCommands();

        assertEquals(CommandStatus.COMPLETED,
                awaitTerminalStatus(descriptor.getCommandId(), Duration.ofSeconds(15)));
    }

    @Test
    void skipsCommandWithLiveRecoveryLease() {
        var descriptor = new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1);
        var entry = journalService.createJournalEntity(descriptor, null);
        entry.status = CommandStatus.RECOVERING;
        entry.recoveryStartedAt = LocalDateTime.now(ZoneOffset.UTC);
        entry.update();

        recoveryService.recoverInterruptedCommands();

        assertEquals(CommandStatus.RECOVERING,
                journalService.findByCommandId(descriptor.getCommandId()).orElseThrow().status);
    }

    private CommandStatus awaitTerminalStatus(String commandId, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        CommandStatus status = null;

        while (Instant.now().isBefore(deadline)) {
            status = journalService.findByCommandId(commandId).map(e -> e.status).orElse(null);
            if (status == CommandStatus.COMPLETED || status == CommandStatus.FAILED) {
                return status;
            }
            sleep();
        }

        return status;
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
