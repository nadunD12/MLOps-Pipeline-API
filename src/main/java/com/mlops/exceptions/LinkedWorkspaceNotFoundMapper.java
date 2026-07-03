package com.mlops.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedWorkspaceNotFoundMapper implements ExceptionMapper<LinkedWorkspaceNotFoundException> {

    @Override
    public Response toResponse(LinkedWorkspaceNotFoundException exception) {
        ErrorResponse er = new ErrorResponse("Unprocessable Entity", exception.getMessage());
        return Response.status(422) // 422 Unprocessable Entity
                .entity(er)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
