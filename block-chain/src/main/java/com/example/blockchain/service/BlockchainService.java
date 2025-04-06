package com.example.blockchain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class BlockchainService {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    
    private final WebClient webClient;
    private final String FASTAPI_BASE_URL = "http://localhost:8011";
    
    public BlockchainService() {
        this.webClient = WebClient.builder()
                .baseUrl(FASTAPI_BASE_URL)
                .build();
    }
    
    /**
     * Check the integrity of the blockchain
     * @return true if the blockchain is valid, false otherwise
     */
    public Mono<Boolean> checkChainIntegrity() {
        logger.info("Checking blockchain integrity at: {}/check_chain_integrity", FASTAPI_BASE_URL);
        
        return webClient.get()
                .uri("/check_chain_integrity")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    // Python API now returns proper JSON
                    Boolean isValid = (Boolean) response.get("Integrity");
                    logger.info("Blockchain integrity check result: {}", isValid);
                    return isValid != null && isValid;
                })
                .onErrorResume(e -> {
                    logger.error("Error checking blockchain integrity: {}", e.getMessage());
                    return Mono.just(false);
                });
    }
    
    /**
     * Store data in the blockchain
     * @param data the data to store
     * @return the response from the FastAPI service
     */
    public Mono<Map<String, Object>> storeData(Map<String, Object> data) {
        logger.info("Storing data in blockchain at: {}/store_data", FASTAPI_BASE_URL);
        
        // Send data directly - the Python API now handles it correctly
        return webClient.post()
                .uri("/store_data")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    logger.info("Data successfully stored in blockchain: {}", response);
                    return response;
                })
                .onErrorResume(e -> {
                    logger.error("Error storing data in blockchain: {}", e.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to store data: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
    }
} 