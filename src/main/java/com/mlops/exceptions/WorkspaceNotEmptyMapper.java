package com.mlops.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WorkspaceNotEmptyMapper implements ExceptionMapper<WorkspaceNotEmptyException> {

    @Override
    public Response toResponse(WorkspaceNotEmptyException exception) {
        ErrorResponse er = new ErrorResponse("Conflict", exception.getMessage());
        return Response.status(Response.Status.CONFLICT) // 409
                .entity(er)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
