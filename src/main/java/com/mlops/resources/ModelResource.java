package com.mlops.resources;

import com.mlops.dao.DataStore;
import com.mlops.exceptions.LinkedWorkspaceNotFoundException;
import com.mlops.models.MLWorkspace;
import com.mlops.models.MachineLearningModel;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    @POST
    public Response createModel(MachineLearningModel model) {
        if (model.getWorkspaceId() == null || !DataStore.getWorkspaces().containsKey(model.getWorkspaceId())) {
            throw new LinkedWorkspaceNotFoundException("Provided workspaceId does not exist.");
        }

        if (model.getId() == null || model.getId().isEmpty()) {
            model.setId(UUID.randomUUID().toString());
        }

        DataStore.getModels().put(model.getId(), model);

        // Link model to workspace
        MLWorkspace ws = DataStore.getWorkspaces().get(model.getWorkspaceId());
        ws.getModelIds().add(model.getId());

        return Response.status(Response.Status.CREATED).entity(model).build();
    }

    @GET
    public Response getModels(@QueryParam("status") String statusFilter) {
        List<MachineLearningModel> result = DataStore.getModels().values().stream()
                .filter(m -> (statusFilter == null || statusFilter.equalsIgnoreCase(m.getStatus())))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET
    @Path("/{modelId}")
    public Response getModel(@PathParam("modelId") String modelId) {
        MachineLearningModel model = DataStore.getModels().get(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(model).build();
    }

    @PUT
    @Path("/{modelId}")
    public Response updateModel(@PathParam("modelId") String modelId, MachineLearningModel updatedModel) {
        MachineLearningModel existing = DataStore.getModels().get(modelId);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (updatedModel.getFramework() != null) {
            existing.setFramework(updatedModel.getFramework());
        }
        if (updatedModel.getStatus() != null) {
            existing.setStatus(updatedModel.getStatus());
        }
        return Response.ok(existing).build();
    }

    // Sub-Resource Locator Pattern implementation
    @Path("/{modelId}/metrics")
    public EvaluationMetricResource getEvaluationMetricResource(@PathParam("modelId") String modelId) {
        return new EvaluationMetricResource(modelId);
    }
}
