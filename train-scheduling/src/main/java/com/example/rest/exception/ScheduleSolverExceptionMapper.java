package com.example.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps ScheduleSolverException to HTTP 404 responses.
 */
@Provider
public class ScheduleSolverExceptionMapper implements ExceptionMapper<ScheduleSolverException> {

    @Override
    public Response toResponse(ScheduleSolverException exception) {
        ErrorInfo errorInfo = new ErrorInfo(exception.getJobId(), exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorInfo)
                .build();
    }
}
