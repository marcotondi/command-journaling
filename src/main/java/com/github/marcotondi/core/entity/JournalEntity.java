package com.github.marcotondi.core.entity;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<Document> commands;
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

        public JournalEntity(
            String commandId,
            String commandType,
            Document payload,
            List<Document> commands,
            LocalDateTime startTime,
            String status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.payload = payload;
        this.commands = commands;
        this.startTime = startTime;
        this.status = status;
    }

}
