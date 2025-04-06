package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/{service}")
    public Mono<ResponseEntity<Map<String, String>>> serviceFallback(@PathVariable String service) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", getServiceName(service) + " Service is currently unavailable");
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response));
    }
    
    private String getServiceName(String service) {
        switch (service) {
            case "auth":
                return "Authentication";
            case "documents":
                return "Document";
            case "analytics":
                return "Analytics";
            case "audit":
                return "Audit";
            case "blockchain":
                return "Blockchain";
            default:
                return "Unknown";
        }
    }
} 