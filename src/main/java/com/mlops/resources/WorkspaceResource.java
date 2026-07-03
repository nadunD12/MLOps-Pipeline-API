package com.mlops.resources;

import com.mlops.dao.DataStore;
import com.mlops.exceptions.WorkspaceNotEmptyException;
import com.mlops.models.MLWorkspace;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    @GET
    public Response getAllWorkspaces() {
        List<MLWorkspace> workspaceList = new ArrayList<>(DataStore.getWorkspaces().values());
        
        CacheControl cc = new CacheControl();
        cc.setMaxAge(60); 
        cc.setPrivate(false);

        return Response.ok(workspaceList).cacheControl(cc).build();
    }

    @POST
    public Response createWorkspace(MLWorkspace workspace) {
        if (workspace.getId() == null || workspace.getId().isEmpty()) {
            workspace.setId(UUID.randomUUID().toString()); // Generate server-side UUID
        }
        DataStore.getWorkspaces().put(workspace.getId(), workspace);
        return Response.status(Response.Status.CREATED).entity(workspace).build();
    }

    @GET
    @Path("/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = DataStore.getWorkspaces().get(workspaceId);
        if (ws == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(ws).build();
    }

    @HEAD
    @Path("/{workspaceId}")
    public Response checkWorkspaceExists(@PathParam("workspaceId") String workspaceId) {
        if (DataStore.getWorkspaces().containsKey(workspaceId)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = DataStore.getWorkspaces().get(workspaceId);
        if (ws == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (ws.getModelIds() != null && !ws.getModelIds().isEmpty()) {
            throw new WorkspaceNotEmptyException("Workspace contains models and cannot be deleted");
        }

        DataStore.getWorkspaces().remove(workspaceId);
        return Response.noContent().build();
    }
}
