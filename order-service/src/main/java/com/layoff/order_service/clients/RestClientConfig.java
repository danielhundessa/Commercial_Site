package com.layoff.order_service.clients;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Autowired(required = false)
    private ObservationRegistry observationRegistry;

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        RestClient.Builder builder = RestClient.builder();

        // Use Spring's built-in observation support which automatically:
        // 1. Injects trace context headers
        // 2. Sets remote service names for dependency visualization in Zipkin
        // 3. Creates proper parent-child span relationships
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }

        return builder;
    }
}