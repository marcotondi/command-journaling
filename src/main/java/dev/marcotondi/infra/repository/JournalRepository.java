package dev.marcotondi.infra.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import dev.marcotondi.domain.entry.JournalEntry;
import dev.marcotondi.domain.model.CommandStatus;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JournalRepository implements PanacheMongoRepository<JournalEntry> {

    public List<JournalEntry> findByCommandType(String commandType) {
        return find("commandType", commandType).list();
    }

    public List<JournalEntry> findByCommandId(String commandId) {
        return find("commandId", commandId).list();
    }

    public List<JournalEntry> findFailedCommands() {
        return find("status", CommandStatus.FAILED).list();
    }

    public List<JournalEntry> findByActor(String actor) {
        return find("actor", actor).list();
    }

    public List<JournalEntry> findInterruptedCommands() {
        return find("status IN ?1",
                Arrays.asList(CommandStatus.PENDING.name(), CommandStatus.EXECUTING.name()))
                .list();
    }

    public Map<String, Integer> getCommandStatistics(LocalDateTime from, LocalDateTime to) {
        MongoCollection<Document> collection = getMongoCollection();

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("startTime", new Document("$gte", from).append("$lte", to))),
                new Document("$group", new Document("_id", "$commandType").append("count", new Document("$sum", 1))));

        AggregateIterable<Document> result = collection.aggregate(pipeline);
        Map<String, Integer> stats = new HashMap<>();
        for (Document doc : result) {
            stats.put(doc.getString("_id"), doc.getInteger("count"));
        }
        return stats;
    }

    private MongoCollection<Document> getMongoCollection() {
        return mongoDatabase().getCollection("command_journal");
    }

    public Double getAverageExecutionTime(String commandType) {
        MongoCollection<Document> collection = getMongoCollection();

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("commandType", commandType).append("status", "COMPLETED")),
                new Document("$group", new Document("_id", null).append("avgExecutionTime",
                        new Document("$avg", "$executionTimeMs"))));

        AggregateIterable<Document> result = collection.aggregate(pipeline);

        Document res = result.first();
        if (res != null && res.getDouble("avgExecutionTime") != null) {
            return res.getDouble("avgExecutionTime");
        } else {
            return 0.0;
        }
    }
}
