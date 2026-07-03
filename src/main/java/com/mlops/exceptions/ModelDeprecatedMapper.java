package com.mlops.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ModelDeprecatedMapper implements ExceptionMapper<ModelDeprecatedException> {

    @Override
    public Response toResponse(ModelDeprecatedException exception) {
        ErrorResponse er = new ErrorResponse("Forbidden", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN) // 403
                .entity(er)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
