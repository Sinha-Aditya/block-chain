package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private final WebClient webClient;
    private final Map<String, Boolean> serviceStatus = new ConcurrentHashMap<>();
    
    private static final Map<String, String> SERVICES = new HashMap<>();
    
    static {
        SERVICES.put("auth", "http://localhost:8081/actuator/health");
        SERVICES.put("documents", "http://localhost:8083/actuator/health");
        SERVICES.put("analytics", "http://localhost:8082/actuator/health");
        SERVICES.put("audit", "http://localhost:8084/actuator/health");
        SERVICES.put("blockchain", "http://localhost:8086/health");
    }
    
    public HealthCheckService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        SERVICES.keySet().forEach(service -> serviceStatus.put(service, true));
    }
    
    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void checkServicesHealth() {
        SERVICES.forEach((service, url) -> {
            checkServiceHealth(service, url)
                .subscribe(
                    isUp -> {
                        boolean previousStatus = serviceStatus.get(service);
                        if (previousStatus != isUp) {
                            logger.info("Service {} status changed from {} to {}", 
                                    service, previousStatus, isUp);
                        }
                        serviceStatus.put(service, isUp);
                    },
                    error -> {
                        logger.error("Error checking health for {}: {}", service, error.getMessage());
                        serviceStatus.put(service, false);
                    }
                );
        });
    }
    
    private Mono<Boolean> checkServiceHealth(String service, String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> response.contains("UP"))
                .onErrorReturn(false);
    }
    
    public boolean isServiceUp(String service) {
        return serviceStatus.getOrDefault(service, false);
    }
} 