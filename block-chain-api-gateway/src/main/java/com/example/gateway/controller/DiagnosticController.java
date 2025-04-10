package com.example.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class DiagnosticController {

    private final DiscoveryClient discoveryClient;
    private final RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    public DiagnosticController(DiscoveryClient discoveryClient, RouteDefinitionLocator routeDefinitionLocator) {
        this.discoveryClient = discoveryClient;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    @GetMapping("/routes")
    public Mono<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        
        return routeDefinitionLocator.getRouteDefinitions()
            .collectList()
            .map(definitions -> {
                response.put("routeCount", definitions.size());
                response.put("routes", definitions.stream()
                        .map(def -> Map.of(
                                "id", def.getId(),
                                "uri", def.getUri().toString(),
                                "predicates", def.getPredicates().stream()
                                        .map(pred -> pred.getName() + ": " + pred.getArgs())
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList()));
                return response;
            });
    }
} 