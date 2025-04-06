package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Route
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("http://localhost:8081"))
                
                // Document Service Route
                .route("documents-service", r -> r.path("/documents/**")
                        .uri("http://localhost:8083"))
                
                // Analytics Service Route
                .route("analytics-service", r -> r.path("/analytics/**")
                        .uri("http://localhost:8082"))
                
                // Audit Service Route
                .route("audit-service", r -> r.path("/audit/**")
                        .uri("http://localhost:8084"))
                
                // Blockchain Service Route
                .route("blockchain-service", r -> r.path("/blockchain/**")
                        .uri("http://localhost:8086"))
                
                // Fallback routes
                .route("fallback-route", r -> r.path("/fallback/**")
                        .uri("forward:/fallback"))
                        
                .build();
    }
} 