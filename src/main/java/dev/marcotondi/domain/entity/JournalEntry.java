package dev.marcotondi.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.codecs.pojo.annotations.BsonId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.domain.api.CommandDescriptor;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
public class JournalEntry extends PanacheMongoEntity {

    public String commandId;
    public String commandType;
    public String actor;
    public String commandPayload;
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
            String commandType,
            String actor,
            String commandPayload,
            LocalDateTime startTime,
            String status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.actor = actor;
        this.commandPayload = commandPayload;
        this.startTime = startTime;
        this.status = status;
    }

    // This constructor is now mainly for reference, the logic will be in the service
    public JournalEntry(
            String commandId,
            String commandType,
            String actor,
            CommandDescriptor commandPayload,
            ObjectMapper objectMapper,
            LocalDateTime startTime,
            String status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.actor = actor;
        try {
            this.commandPayload = objectMapper.writeValueAsString(commandPayload);
        } catch (JsonProcessingException e) {
            // In a real app, you'd want more robust error handling
            this.commandPayload = "{\"error\":\"Failed to serialize command payload\"}";
        }
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
