package com.example.mailsender.controller;

import com.example.mailsender.service.BlockchainIntegrityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StatusController {

    @Autowired
    private BlockchainIntegrityService integrityService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getServiceStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "block-chain-mail-sender");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/check-integrity")
    public Mono<ResponseEntity<?>> checkIntegrityManually() {
        return integrityService.checkBlockchainIntegrity()
                .map(response -> ResponseEntity.ok(response));
    }
} 