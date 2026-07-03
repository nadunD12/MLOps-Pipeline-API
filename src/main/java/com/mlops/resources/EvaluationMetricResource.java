package com.mlops.resources;

import com.mlops.dao.DataStore;
import com.mlops.exceptions.ModelDeprecatedException;
import com.mlops.models.EvaluationMetric;
import com.mlops.models.MachineLearningModel;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationMetricResource {

    private final String modelId;

    public EvaluationMetricResource(String modelId) {
        this.modelId = modelId;
    }

    @GET
    public Response getMetricsHistory() {
        List<EvaluationMetric> metricsList = DataStore.getMetrics().getOrDefault(modelId, new ArrayList<>());
        return Response.ok(metricsList).build();
    }

    @POST
    public Response addMetric(EvaluationMetric metric) {
        MachineLearningModel model = DataStore.getModels().get(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if ("DEPRECATED".equalsIgnoreCase(model.getStatus())) {
            throw new ModelDeprecatedException("Cannot add metrics to a DEPRECATED model.");
        }

        if (metric.getId() == null || metric.getId().isEmpty()) {
            metric.setId(UUID.randomUUID().toString());
        }
        if (metric.getTimestamp() == 0) {
            metric.setTimestamp(System.currentTimeMillis());
        }

        List<EvaluationMetric> metricsList = DataStore.getMetrics().computeIfAbsent(modelId, k -> new ArrayList<>());
        metricsList.add(metric);

        // Side Effect: Update the latestAccuracy field on the parent Model object
        model.setLatestAccuracy(metric.getAccuracyScore());

        return Response.status(Response.Status.CREATED).entity(metric).build();
    }
}
