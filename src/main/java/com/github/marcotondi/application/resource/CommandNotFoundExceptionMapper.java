package com.github.marcotondi.application.resource;

import com.github.marcotondi.core.domain.exception.CommandNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CommandNotFoundExceptionMapper implements ExceptionMapper<CommandNotFoundException> {

    @Override
    public Response toResponse(CommandNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(exception.getMessage())
                .build();
    }
}
