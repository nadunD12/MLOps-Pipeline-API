package com.mlops.dao;

import com.mlops.models.EvaluationMetric;
import com.mlops.models.MLWorkspace;
import com.mlops.models.MachineLearningModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final Map<String, MLWorkspace> workspaces = new ConcurrentHashMap<>();
    private static final Map<String, MachineLearningModel> models = new ConcurrentHashMap<>();
    private static final Map<String, List<EvaluationMetric>> metrics = new ConcurrentHashMap<>();

    public static Map<String, MLWorkspace> getWorkspaces() {
        return workspaces;
    }

    public static Map<String, MachineLearningModel> getModels() {
        return models;
    }

    public static Map<String, List<EvaluationMetric>> getMetrics() {
        return metrics;
    }
}
