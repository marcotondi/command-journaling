package dev.marcotondi.core.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalEntity extends PanacheMongoEntity {

    public String commandId;
    public String commandType;
    public Document payload;
    public String status;

    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Long executionTimeMs;

    public Document result;
    public String errorMessage;

    public Map<String, String> metadata;

    public JournalEntity() { }

    public JournalEntity(
            String commandId,
            String commandType,
            Document payload,
            LocalDateTime startTime,
            String status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.payload = payload;
        this.startTime = startTime;
        this.status = status;
    }

}
