package com.mlops.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual exception for server context, but hide from client (Part 5 requirement)
        exception.printStackTrace();
        
        ErrorResponse er = new ErrorResponse("Internal Server Error", "An unexpected error occurred.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                .entity(er)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
