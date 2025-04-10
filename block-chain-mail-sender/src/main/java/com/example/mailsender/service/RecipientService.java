package com.example.mailsender.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipientService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecipientService.class);
    
    @Value("${blockchain.integrity.alert.emails:#{null}}")
    private String configuredEmails;
    
    private final List<String> recipients = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Initialize recipients from configuration
        if (configuredEmails != null && !configuredEmails.trim().isEmpty()) {
            Arrays.stream(configuredEmails.split(","))
                  .map(String::trim)
                  .filter(email -> !email.isEmpty())
                  .forEach(this::addRecipient);
        }
        logger.info("Initialized email recipients: {}", recipients);
    }
    
    public List<String> getAllRecipients() {
        return new ArrayList<>(recipients);
    }
    
    public boolean addRecipient(String email) {
        // Basic email validation
        if (email == null || !email.matches(".+@.+\\..+")) {
            logger.warn("Invalid email format: {}", email);
            return false;
        }
        
        if (!recipients.contains(email)) {
            recipients.add(email);
            logger.info("Added email recipient: {}", email);
            return true;
        }
        return false;
    }
    
    public boolean removeRecipient(String email) {
        boolean removed = recipients.remove(email);
        if (removed) {
            logger.info("Removed email recipient: {}", email);
        }
        return removed;
    }
    
    public int getRecipientCount() {
        return recipients.size();
    }
} 