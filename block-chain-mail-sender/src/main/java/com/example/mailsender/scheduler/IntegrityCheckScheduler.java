package com.example.mailsender.scheduler;

import com.example.mailsender.model.IntegrityCheckResponse;
import com.example.mailsender.service.BlockchainIntegrityService;
import com.example.mailsender.service.EmailService;
import com.example.mailsender.service.RecipientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IntegrityCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityCheckScheduler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private BlockchainIntegrityService integrityService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RecipientService recipientService;
    
    private boolean lastIntegrityStatus = true;
    
    // Approach 1: Cron expression - runs every minute
    @Scheduled(cron = "0 * * * * ?")
    public void checkIntegrityWithCron() {
        System.out.println("CRON CHECK - " + System.currentTimeMillis());
        performIntegrityCheck();
    }
    
    // Approach 2: Fixed delay (runs after previous execution completes)
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void checkIntegrityWithFixedDelay() {
        System.out.println("FIXED DELAY CHECK - " + System.currentTimeMillis());
        performIntegrityCheck();
    }
    
    // Approach 3: Fixed rate (tries to maintain consistent interval)
    @Scheduled(fixedRate = 60000) // 1 minute
    public void checkIntegrityWithFixedRate() {
        System.out.println("FIXED RATE CHECK - " + System.currentTimeMillis());
        performIntegrityCheck();
    }
    
    private void performIntegrityCheck() {
        logger.info("Starting scheduled blockchain integrity check");
        
        integrityService.checkBlockchainIntegrity()
            .subscribe(response -> {
                if (response.getIntegrity() == null) {
                    logger.error("Failed to determine blockchain integrity status");
                    return;
                }
                
                boolean isIntegrityValid = response.getIntegrity();
                logger.info("Blockchain integrity check result: {}", isIntegrityValid ? "VALID" : "COMPROMISED");
                
                // If integrity is false, send an alert
                if (!isIntegrityValid) {
                    try {
                        String additionalInfo = objectMapper.writeValueAsString(response.getAdditionalInfo());
                        
                        emailService.sendIntegrityAlertEmail(additionalInfo);
                        
                        // Only log transition from valid to invalid
                        if (lastIntegrityStatus) {
                            logger.warn("⚠️ ALERT: Blockchain integrity compromised! " +
                                       "Alert emails sent to {} recipients", recipientService.getRecipientCount());
                        }
                    } catch (Exception e) {
                        logger.error("Error processing integrity check response: {}", e.getMessage(), e);
                    }
                } else if (!lastIntegrityStatus) {
                    // Log recovery from invalid to valid state
                    logger.info("Blockchain integrity restored to valid state");
                }
                
                // Update last known status
                lastIntegrityStatus = isIntegrityValid;
            }, error -> {
                // Add explicit error handling
                logger.error("Error during scheduled integrity check: {}", error.getMessage(), error);
            });
    }
} 