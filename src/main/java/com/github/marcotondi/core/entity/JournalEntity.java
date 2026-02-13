package com.github.marcotondi.core.entity;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import com.github.marcotondi.core.CommandStatus;
import com.github.marcotondi.core.CoreKeys;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = JournalEntity.COLLECTION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalEntity extends PanacheMongoEntity {

    public static final String COLLECTION = "command_journal";

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

    /** Set when a process claims the command for recovery/restart; stale claims can be taken over. */
    public LocalDateTime recoveryStartedAt;

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
    public static List<JournalEntity> findInterruptedCommands() {
        return find("status IN ?1",
                Arrays.asList(
                        CommandStatus.PENDING.name(),
                        CommandStatus.EXECUTING.name(),
                        CommandStatus.RECOVERING.name(),
                        CommandStatus.EXECUTING_ROLL_BACK.name()))
                .list();
    }

    /**
     * Atomically claims the command by moving it to RECOVERING, but only if it is
     * still in one of the given statuses and no live recovery lease is held.
     * Returns true only for the caller that won.
     */
    public static boolean claim(String commandId, Collection<String> fromStatuses, LocalDateTime staleBefore) {
        UpdateResult result = getMongoCollection().updateOne(
                Filters.and(
                        Filters.eq(CoreKeys.COMMAND_ID, commandId),
                        Filters.in(CoreKeys.STATUS, fromStatuses),
                        Filters.or(
                                Filters.exists("recoveryStartedAt", false),
                                Filters.lt("recoveryStartedAt", staleBefore))),
                Updates.combine(
                        Updates.set(CoreKeys.STATUS, CommandStatus.RECOVERING.name()),
                        Updates.set("recoveryStartedAt", LocalDateTime.now(ZoneOffset.UTC))));
        return result.getModifiedCount() > 0;
    }

    /**
     * Creates the unique index that makes commandId the journal's identity key.
     * Idempotent: MongoDB ignores the call when the index already exists.
     */
    public static void ensureCommandIdIndex() {
        getMongoCollection().createIndex(
                Indexes.ascending(CoreKeys.COMMAND_ID),
                new IndexOptions().unique(true));
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
        return mongoDatabase().getCollection(COLLECTION);
    }

}
