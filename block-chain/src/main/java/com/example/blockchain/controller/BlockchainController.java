package com.example.blockchain.controller;

import com.example.blockchain.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/blockchain")
public class BlockchainController {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainController.class);
    
    private final BlockchainService blockchainService;
    
    @Autowired
    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }
    
    /**
     * Store data in the blockchain after checking integrity
     * @param data the data to store
     * @return the response with stored data details
     */
    @PostMapping("/store")
    public Mono<ResponseEntity<Map<String, Object>>> storeData(@RequestBody Map<String, Object> data) {
        logger.info("Received request to store data in blockchain: {}", data);
        
        // First check chain integrity
        return blockchainService.checkChainIntegrity()
                .flatMap(isValid -> {
                    if (!isValid) {
                        logger.warn("Blockchain integrity check failed - aborting store operation");
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("error", "Blockchain integrity check failed - data not stored");
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                    }
                    
                    // Chain is valid, proceed to store data
                    logger.info("Blockchain integrity check passed - proceeding to store data");
                    return blockchainService.storeData(data)
                            .map(response -> {
                                // Check if the response indicates success
                                String message = (String) response.get("message");
                                if (message != null && message.contains("successfully")) {
                                    return ResponseEntity.ok(response);
                                } else {
                                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                                }
                            });
                });
    }
    
    /**
     * Check the integrity of the blockchain
     * @return the response with blockchain validity status
     */
    @GetMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateChain() {
        logger.info("Received request to validate blockchain");
        
        return blockchainService.checkChainIntegrity()
                .map(isValid -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", isValid);
                    return ResponseEntity.ok(response);
                });
    }
    
    /**
     * Health check endpoint
     * @return the health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
} 