package dev.marcotondi.application.todo.client;

import dev.marcotondi.application.todo.entity.TodoEntity;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;

@Path("/todos")
@RegisterRestClient(configKey = "todos-api")
public interface TodoRestClient {

    @GET
    @Produces("application/json")
    List<TodoEntity> getAll();

    @GET
    @Path("/{id}")
    @Produces("application/json")
    TodoEntity getById(@PathParam("id") Integer id);
}
