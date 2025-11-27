package dev.marcotondi.application.todo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import dev.marcotondi.application.todo.client.TodoRestClient;
import dev.marcotondi.application.todo.entity.TodoEntity;
import dev.marcotondi.application.todo.model.TodoDescriptor;
import dev.marcotondi.core.api.CommandType;
import dev.marcotondi.core.api.CommandTypeName;
import dev.marcotondi.core.domain.Command;
import dev.marcotondi.core.domain.CommandDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CommandType("TODO")
public class TodoCommand extends Command<List<TodoEntity>> {

    private static final Logger LOG = Logger.getLogger(TodoCommand.class);

    @Inject
    @RestClient
    TodoRestClient todoRestClient;

    private Integer todoId = null;

    @Override
    public List<TodoEntity> doExecute() {
        try {
            if (todoId == null) {
                LOG.info("Calling remote service to get all todos");
                List<TodoEntity> todos = todoRestClient.getAll();
                LOG.infof("Recuperati %d todo", todos.size());
                return todos;
            } else {
                LOG.infof("Calling remote service to get todo with id %d", todoId);
                TodoEntity single = todoRestClient.getById(todoId);
                return Collections.singletonList(single);
            }
        } catch (Exception e) {
            LOG.error("Errore chiamando il servizio rest", e);
            throw new RuntimeException("Errore chiamando il servizio rest", e);
        }
    }

    @Override
    public List<TodoEntity> doUndo() {
        // Nessuna operazione di undo richiesta
        LOG.info("TodoCommand.doUndo() chiamato: nessuna azione eseguita");
        return Collections.emptyList();
    }

    @Override
    public CommandDescriptor setDescriptor(Map<String, Object> payload) {

        var descriptor = new TodoDescriptor(
                UUID.fromString((String) payload.get("commandId")),
                LocalDateTime.parse((String) payload.get("timestamp")),
                CommandTypeName.TODO,
                (String) payload.get("actor"));

        if (payload.containsKey("todoId")) {
            this.todoId = (Integer) payload.get("todoId");
        }

        super.setDescriptor(descriptor);
        return descriptor;
    }

}
