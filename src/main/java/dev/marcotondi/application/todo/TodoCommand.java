package dev.marcotondi.application.todo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.marcotondi.application.todo.entity.TodoEntity;
import dev.marcotondi.application.todo.model.TodoDescriptor;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CommandType("TODO")
public class TodoCommand extends Command<List<TodoEntity>> {

    private static final Logger LOG = Logger.getLogger(TodoCommand.class);

    private static final String BASE_URL = "http://localhost:8000/todos";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Integer todoId = null;

    @Override
    public CommandDescriptor descriptorFromJournal(Map<String, Object> payload, LocalDateTime time) {
        return new TodoDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                time, // ((String) payload.get("timestamp")),
                CommandTypeName.DELETE_USER,
                (String) payload.get("actor"));
    }

    @Override
    public List<TodoEntity> doExecute() {
        try {
            String url = (todoId == null)
                    ? BASE_URL
                    : BASE_URL + "/" + todoId;

            LOG.infof("Calling JSONPlaceholder: %s", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Chiamata fallita, status: " + response.statusCode());
            }

            String body = response.body();

            // Con id nullo: /todos -> array JSON
            if (todoId == null) {
                List<TodoEntity> todos = objectMapper.readValue(
                        body, new TypeReference<List<TodoEntity>>() {
                        });

                LOG.infof("Recuperati %d todo", todos.size());
                return todos;
            }

            // Con id valorizzato: /todos/{id} -> singolo oggetto JSON
            TodoEntity single = objectMapper.readValue(body, TodoEntity.class);
            return Collections.singletonList(single);

        } catch (IOException | InterruptedException e) {
            LOG.error("Errore chiamando JSONPlaceholder", e);
            throw new RuntimeException("Errore chiamando JSONPlaceholder", e);
        }
    }

    @Override
    public List<TodoEntity> doUndo() {
        // Nessuna operazione di undo richiesta
        LOG.info("TodoCommand.doUndo() chiamato: nessuna azione eseguita");
        return Collections.emptyList();
    }

}
