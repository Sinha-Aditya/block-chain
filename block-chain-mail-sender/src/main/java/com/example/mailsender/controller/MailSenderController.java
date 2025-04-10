package com.example.mailsender.controller;

import com.example.mailsender.service.BlockchainIntegrityService;
import com.example.mailsender.service.EmailService;
import com.example.mailsender.service.RecipientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mail")
public class MailSenderController {

    private static final Logger logger = LoggerFactory.getLogger(MailSenderController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BlockchainIntegrityService integrityService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RecipientService recipientService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "mail-sender-service");
        status.put("status", "UP");
        status.put("recipient_count", String.valueOf(recipientService.getRecipientCount()));
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/check-integrity")
    public Mono<ResponseEntity<Map<String, Object>>> checkIntegrity() {
        logger.info("Manual integrity check triggered via API Gateway");
        
        return integrityService.checkBlockchainIntegrity()
            .map(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", System.currentTimeMillis());
                response.put("integrity_status", result.getIntegrity());
                response.put("is_tampered", result.getIntegrity() != null ? !result.getIntegrity() : "unknown");
                
                if (result.getAdditionalInfo() != null) {
                    response.put("details", result.getAdditionalInfo());
                }
                
                // If integrity is compromised, send an email alert to all recipients
                if (result.getIntegrity() != null && !result.getIntegrity()) {
                    try {
                        String additionalInfo = objectMapper.writeValueAsString(result.getAdditionalInfo());
                        emailService.sendIntegrityAlertEmail(additionalInfo);
                        response.put("alert_sent", true);
                        response.put("recipient_count", recipientService.getRecipientCount());
                        logger.warn("Alert emails sent to {} recipients", recipientService.getRecipientCount());
                    } catch (Exception e) {
                        response.put("alert_sent", false);
                        response.put("alert_error", e.getMessage());
                        logger.error("Failed to send alert emails: {}", e.getMessage());
                    }
                }
                
                return ResponseEntity.ok(response);
            })
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "error", "Failed to check blockchain integrity",
                    "message", "The integrity check service may be unavailable"
                ))
            );
    }
    
    // New endpoints for managing recipients
    
    @GetMapping("/recipients")
    public ResponseEntity<Map<String, Object>> getRecipients() {
        Map<String, Object> response = new HashMap<>();
        response.put("recipients", recipientService.getAllRecipients());
        response.put("count", recipientService.getRecipientCount());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/recipients")
    public ResponseEntity<Map<String, Object>> addRecipient(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        if (!request.containsKey("email")) {
            response.put("success", false);
            response.put("message", "Email address is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        String email = request.get("email");
        boolean added = recipientService.addRecipient(email);
        
        response.put("success", added);
        response.put("message", added ? 
                "Recipient added successfully" : 
                "Failed to add recipient (invalid format or already exists)");
        response.put("recipients", recipientService.getAllRecipients());
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/recipients/{email}")
    public ResponseEntity<Map<String, Object>> removeRecipient(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        boolean removed = recipientService.removeRecipient(email);
        
        response.put("success", removed);
        response.put("message", removed ? 
                "Recipient removed successfully" : 
                "Recipient not found");
        response.put("recipients", recipientService.getAllRecipients());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        if (!request.containsKey("email")) {
            response.put("success", false);
            response.put("message", "Email address is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        String email = request.get("email");
        boolean sent = emailService.sendTestEmail(email);
        
        response.put("success", sent);
        response.put("message", sent ? 
                "Test email sent successfully" : 
                "Failed to send test email");
        
        return ResponseEntity.ok(response);
    }
} 