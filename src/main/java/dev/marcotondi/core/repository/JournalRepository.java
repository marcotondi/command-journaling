package dev.marcotondi.core.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import dev.marcotondi.core.entity.JournalEntity;
import dev.marcotondi.core.CommandStatus;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JournalRepository implements PanacheMongoRepository<JournalEntity> {

    // ------------------------------------------------------------
    // Query by identifiers
    // ------------------------------------------------------------
    public Optional<JournalEntity> findByCommandId(String commandId) {
        return find("commandId", commandId).firstResultOptional();
    }

    // ------------------------------------------------------------
    // Query by properties
    // ------------------------------------------------------------
    public List<JournalEntity> findByCommandType(String commandType) {
        return find("commandType", commandType).list();
    }

    public List<JournalEntity> findFailedCommands() {
        return find("status", CommandStatus.FAILED.name()).list();
    }

    public List<JournalEntity> findByActor(String actor) {
        return find("actor", actor).list();
    }

    public List<JournalEntity> findInterruptedCommands() {
        return find("status IN ?1",
                Arrays.asList(
                        CommandStatus.PENDING.name(),
                        CommandStatus.EXECUTING.name(),
                        CommandStatus.EXECUTING_ROLL_BACK.name()
                )).list();
    }

    // ------------------------------------------------------------
    // Mongo Aggregations
    // ------------------------------------------------------------
    public Map<String, Long> getCommandStatistics(LocalDateTime from, LocalDateTime to) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match",
                        new Document("startTime",
                                new Document("$gte", from)
                                .append("$lte", to))),
                new Document("$group",
                        new Document("_id", "$commandType")
                                .append("count", new Document("$sum", 1)))
        );

        Map<String, Long> stats = new HashMap<>();
        for (Document doc : getMongoCollection().aggregate(pipeline)) {
            stats.put(doc.getString("_id"), doc.getLong("count"));
        }
        return stats;
    }

    public double getAverageExecutionTime(String commandType) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match",
                        new Document("commandType", commandType)
                                .append("status", CommandStatus.COMPLETED.name())),
                new Document("$group",
                        new Document("_id", null)
                                .append("avgExecutionTime",
                                        new Document("$avg", "$executionTimeMs")))
        );

        Document res = getMongoCollection().aggregate(pipeline).first();
        return res == null ? 0.0 : res.getDouble("avgExecutionTime");
    }

    private MongoCollection<Document> getMongoCollection() {
        return mongoDatabase().getCollection("command_journal");
    }
}
