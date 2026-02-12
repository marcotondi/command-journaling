package com.github.marcotondi.core.entity;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command_journal")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalEntity extends PanacheMongoEntity {

    public String commandId;
    public String commandType;
    public CommandStatus status;

    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Long executionTimeMs;

    public Document payload;
    public List<Document> commands;

    public Document result;
    public String errorMessage;

    public Map<String, String> metadata;

    public JournalEntity() {
    }

    public JournalEntity(
            String commandId,
            String commandType,
            Document payload,
            LocalDateTime startTime,
            CommandStatus status) {
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
            CommandStatus status) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.payload = payload;
        this.commands = commands;
        this.startTime = startTime;
        this.status = status;
    }

    // ------------------------------------------------------------
    // Query by identifiers
    // ------------------------------------------------------------
    public static Optional<JournalEntity> findByCommandId(String commandId) {
        return find(CoreKeys.COMMAND_ID, commandId).firstResultOptional();
    }

    public static JournalEntity findBySubCommandId(String subCommandId) {
        return find("commands.commandId", subCommandId).firstResult();
    }

    // ------------------------------------------------------------
    // Query by properties
    // ------------------------------------------------------------
    public static List<JournalEntity> findByCommandType(String commandType) {
        return find(CoreKeys.COMMAND_TYPE, commandType).list();
    }

    public static List<JournalEntity> findByActor(String actor) {
        return find(CoreKeys.ACTOR, actor).list();
    }

    public static List<JournalEntity> findByStatus(String status) {
        return find(CoreKeys.STATUS, status).list();
    }

    public static List<JournalEntity> findInterruptedCommands() {
        return find("status IN ?1",
                Arrays.asList(
                        CommandStatus.PENDING.name(),
                        CommandStatus.EXECUTING.name(),
                        CommandStatus.EXECUTING_ROLL_BACK.name()))
                .list();
    }

    // ------------------------------------------------------------
    // Mongo Aggregations
    // ------------------------------------------------------------
    public static Map<String, Long> getCommandStatistics(LocalDateTime from, LocalDateTime to) {
        List<Bson> pipeline = Arrays.asList(
                match(gte("startTime", from)),
                match(lte("startTime", to)),
                group("$commandType", Accumulators.sum("count", 1)));

        Map<String, Long> stats = new HashMap<>();
        for (Document doc : getMongoCollection().aggregate(pipeline)) {
            stats.put(doc.getString("_id"), doc.getLong("count"));
        }
        return stats;
    }

    public static double getAverageExecutionTime(String commandType) {
        try {
            List<Bson> pipeline = Arrays.asList(
                    match(eq(CoreKeys.COMMAND_TYPE, commandType)),
                    match(eq(CoreKeys.STATUS, CommandStatus.COMPLETED.name())),
                    group(null, Accumulators.avg("avgExecutionTime", "$executionTimeMs")));
            Document res = getMongoCollection().aggregate(pipeline).first();
            return res == null ? 0.0 : res.getDouble("avgExecutionTime");
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static MongoCollection<Document> getMongoCollection() {
        return mongoDatabase().getCollection("command_journal");
    }

}
