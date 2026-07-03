package com.mlops.config;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        // Register resources, providers, filters in these packages
        packages("com.mlops.resources", "com.mlops.exceptions", "com.mlops.filters");
    }
}
