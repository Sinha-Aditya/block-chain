package com.example.mailsender.service;

import com.example.mailsender.model.IntegrityCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class BlockchainIntegrityService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainIntegrityService.class);
    
    private final WebClient webClient;
    
    @Value("${blockchain.integrity.url}")
    private String integrityCheckUrl;
    
    @Autowired
    public BlockchainIntegrityService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<IntegrityCheckResponse> checkBlockchainIntegrity() {
        logger.info("Checking blockchain integrity at: {}", integrityCheckUrl);
        
        return webClient.get()
                .uri(integrityCheckUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    IntegrityCheckResponse result = new IntegrityCheckResponse();
                    
                    // Extract the integrity field (case-insensitive)
                    Boolean integrity = null;
                    for (Object key : response.keySet()) {
                        if (key.toString().equalsIgnoreCase("integrity")) {
                            Object value = response.get(key);
                            if (value instanceof Boolean) {
                                integrity = (Boolean) value;
                            } else if (value instanceof String) {
                                integrity = Boolean.parseBoolean((String) value);
                            }
                            break;
                        }
                    }
                    
                    result.setIntegrity(integrity);
                    result.setAdditionalProperties(response);
                    return result;
                })
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> logger.error("Error checking blockchain integrity: {}", error.getMessage(), error))
                .onErrorResume(e -> {
                    IntegrityCheckResponse errorResponse = new IntegrityCheckResponse();
                    errorResponse.setIntegrity(null); // null indicates error
                    return Mono.just(errorResponse);
                });
    }
} 