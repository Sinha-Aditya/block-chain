package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("blockchain-direct", r -> r.path("/blockchain/**")
                        // .uri("http://localhost:8086")) 
                        .uri("lb://block-chain"))
                // Keep the service discovery route as backup
                // .route("blockchain-service", r -> r.path("/blockchain/**")
                //         .uri("lb://blockchain-service"))
                
                // Auth Service Route - Use service discovery instead of direct URL
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://auth-service"))
                
                // Documents Service Route
                .route("documents-service", r -> r.path("/documents/**")
                        .uri("lb://blockchain-service"))
                
                // Analytics Service Route
                .route("analytics-service", r -> r.path("/analytics/**")
                        .uri("lb://analytics-service"))
                
                // Audit Service Route
                .route("audit-service", r -> r.path("/audit/**")
                        .uri("lb://audit-service"))
                
                // Data Retrieve Service Route
                .route("data-retrieve-service", r -> r.path("/data/**")
                        .uri("lb://data-retrieve-service"))
                
                // Fallback routes
                .route("fallback-route", r -> r.path("/fallback/**")
                        .uri("forward:/fallback"))
                
                // Add a route for the mail sender service
                .route("mail-sender-service", r -> r.path("/mail/**")
                        .uri("lb://mail-sender-service"))
                
                .build();
    }
} 