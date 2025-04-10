package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private final DiscoveryClient discoveryClient;

    public HealthCheckService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void checkServiceHealth() {
        logger.info("Performing service health check...");
        
        discoveryClient.getServices().forEach(serviceId -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            if (instances.isEmpty()) {
                logger.warn("No instances found for service: {}", serviceId);
            } else {
                logger.info("Service {} has {} active instance(s)", serviceId, instances.size());
            }
        });
    }
} 