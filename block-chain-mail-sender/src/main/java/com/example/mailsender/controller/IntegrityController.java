package com.example.mailsender.controller;

import com.example.mailsender.model.IntegrityCheckResponse;
import com.example.mailsender.service.BlockchainIntegrityService;
import com.example.mailsender.service.EmailService;
import com.example.mailsender.service.RecipientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integrity")
public class IntegrityController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BlockchainIntegrityService integrityService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RecipientService recipientService;
    
    @GetMapping("/check-and-notify")
    public Mono<ResponseEntity<Map<String, Object>>> checkIntegrityAndNotify() {
        logger.info("Manual integrity check triggered via API");
        
        return integrityService.checkBlockchainIntegrity()
            .map(response -> {
                Map<String, Object> result = new HashMap<>();
                result.put("integrityCheck", response);
                
                if (response.getIntegrity() == null) {
                    result.put("status", "ERROR");
                    result.put("message", "Failed to determine blockchain integrity");
                    return ResponseEntity.ok(result);
                }
                
                boolean isIntegrityValid = response.getIntegrity();
                result.put("status", isIntegrityValid ? "VALID" : "COMPROMISED");
                
                // If integrity is false, send an alert
                if (!isIntegrityValid) {
                    try {
                        String additionalInfo = objectMapper.writeValueAsString(response.getAdditionalInfo());
                        emailService.sendIntegrityAlertEmail(additionalInfo);
                        result.put("emailSent", true);
                        result.put("recipients", recipientService.getAllRecipients());
                        result.put("recipientCount", recipientService.getRecipientCount());
                        logger.warn("⚠️ ALERT: Blockchain integrity compromised! Alert emails sent to {} recipients", 
                                    recipientService.getRecipientCount());
                    } catch (Exception e) {
                        result.put("emailSent", false);
                        result.put("emailError", e.getMessage());
                        logger.error("Error sending integrity alert email: {}", e.getMessage(), e);
                    }
                } else {
                    result.put("emailSent", false);
                    result.put("message", "Blockchain integrity is valid, no alert needed");
                }
                
                return ResponseEntity.ok(result);
            });
    }
} 