package com.github.marcotondi.application.resource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.github.marcotondi.application.sleep.model.SleepDescriptor;
import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import com.github.marcotondi.core.api.JournalService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
class JournalRestartTest {

    @Inject
    JournalService journalService;

    @Test
    void refusesRestartOfCompletedCommand() {
        var descriptor = new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1);
        var entry = journalService.createJournalEntity(descriptor, null);
        journalService.updateJournalOnSuccess(entry, "done", 1L);

        restart(descriptor.getCommandId(), 409);
    }

    @Test
    void restartsFailedCommand() {
        var descriptor = new SleepDescriptor(CoreKeys.SYSTEM_ACTOR, 1);
        var entry = journalService.createJournalEntity(descriptor, null);
        journalService.updateJournalOnFailure(entry, new RuntimeException("boom"));

        restart(descriptor.getCommandId(), 202);

        assertEquals(CommandStatus.COMPLETED,
                awaitTerminalStatus(descriptor.getCommandId(), Duration.ofSeconds(15)));
    }

    private void restart(String commandId, int expectedStatus) {
        given().contentType(ContentType.JSON)
                .body("{\"commandId\":\"" + commandId + "\"}")
                .when().post("/api/journal/restart")
                .then().statusCode(expectedStatus);
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
