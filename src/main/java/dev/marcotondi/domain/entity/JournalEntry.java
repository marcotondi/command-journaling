package dev.marcotondi.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.marcotondi.domain.api.CommandTypeName;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
public class JournalEntry extends PanacheMongoEntity {

    public String commandId;
    public CommandTypeName commandType;
    public int payloadVersion;
    public String actor;
    public String payload;
    public String status;

    public String parentCommandId;
    public List<String> childCommandIds = new ArrayList<>();

    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Long executionTimeMs;

    public String result;
    public String errorMessage;

    public String sourceIp;
    public String sessionId;
    public Map<String, String> metadata;

    public JournalEntry() {
    }

    public JournalEntry(
            String commandId,
            CommandTypeName commandType,
            int payloadVersion,
            String actor,
            String payload,
            LocalDateTime startTime,
            String status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.payloadVersion = payloadVersion;
        this.actor = actor;
        this.payload = payload;
        this.startTime = startTime;
        this.status = status;
    }

    // Metodo helper per verificare se è un composito
    public boolean isComposite() {
        return !childCommandIds.isEmpty();
    }

    // Metodo helper per verificare se è un comando figlio
    public boolean isChild() {
        return parentCommandId != null;
    }

}
