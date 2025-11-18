package dev.marcotondi.core.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.marcotondi.core.api.CommandTypeName;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalEntity extends PanacheMongoEntity {

    public String commandId;
    public CommandTypeName commandType;
    public int payloadVersion;
    public String actor;
    public String payload;
    public String status;

    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Long executionTimeMs;

    public String result;
    public String errorMessage;

    public String sourceIp;
    public String sessionId;
    public Map<String, String> metadata;

    public JournalEntity() { }

    public JournalEntity(
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

}
