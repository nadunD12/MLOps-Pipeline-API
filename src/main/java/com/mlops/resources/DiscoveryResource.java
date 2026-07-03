package com.mlops.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("api_version", "v1.0");
        metadata.put("administrative_contact", "admin@mlops.com");
        
        Map<String, String> collections = new HashMap<>();
        collections.put("workspaces", "/api/v1/workspaces");
        collections.put("models", "/api/v1/models");
        
        metadata.put("primary_collections", collections);
        return Response.ok(metadata).build();
    }
}
