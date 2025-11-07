package dev.marcotondi.domain.entry;

import java.time.LocalDateTime;
import java.util.Map;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
public class JournalEntry extends PanacheMongoEntity {

    private String commandId;
    private String commandType;
    private String actor;
    private String commandPayload;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String result;
    private String errorDetails;
    private Long executionTimeMs;
    public String sourceIp;
    private String sessionId;
    private Map<String, String> metadata;

    public JournalEntry() { }

    public JournalEntry(
                String commandId, 
                String commandType, 
                String actor, 
                String commandPayload,
                LocalDateTime startTime, 
                LocalDateTime endTime, 
                String status, 
                String result, 
                String errorDetails,
                Long executionTimeMs, 
                String sourceIp, 
                String sessionId, 
                Map<String, String> metadata
    ) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.actor = actor;
        this.commandPayload = commandPayload;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.result = result;
        this.errorDetails = errorDetails;
        this.executionTimeMs = executionTimeMs;
        this.sourceIp = sourceIp;
        this.sessionId = sessionId;
        this.metadata = metadata;
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
        return this.commandPayload;
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
