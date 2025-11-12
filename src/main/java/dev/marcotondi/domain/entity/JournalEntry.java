package dev.marcotondi.domain.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.domain.api.CommandDescriptor;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
public class JournalEntry extends PanacheMongoEntity {

    private String commandId;
    private String commandType;
    private String actor;
    private String commandPayload; // Added field for serialized payload
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String result;
    private String errorDetails;
    private Long executionTimeMs;
    public String sourceIp;
    private String sessionId;
    private Map<String, String> metadata;

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
            CommandDescriptor<?> commandPayload,
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


    public String getCommandId() {
        return this.commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getCommandType() {
        return this.commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getActor() {
        return this.actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getCommandPayload() {
        return commandPayload;
    }

    public void setCommandPayload(String commandPayload) {
        this.commandPayload = commandPayload;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorDetails() {
        return this.errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Long getExecutionTimeMs() {
        return this.executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getSourceIp() {
        return this.sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "{" +
                " commandId='" + getCommandId() + "'" +
                ", commandType='" + getCommandType() + "'" +
                ", actor='" + getActor() + "'" +
                ", commandPayload='" + getCommandPayload() + "'" +
                ", startTime='" + getStartTime() + "'" +
                ", endTime='" + getEndTime() + "'" +
                ", status='" + getStatus() + "'" +
                ", result='" + getResult() + "'" +
                ", errorDetails='" + getErrorDetails() + "'" +
                ", executionTimeMs='" + getExecutionTimeMs() + "'" +
                ", sourceIp='" + getSourceIp() + "'" +
                ", sessionId='" + getSessionId() + "'" +
                ", metadata='" + getMetadata() + "'" +
                "}";
    }

}
