package com.example.mailsender.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private RecipientService recipientService;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    public void sendIntegrityAlertEmail(String additionalInfo) {
        List<String> recipients = recipientService.getAllRecipients();
        if (recipients.isEmpty()) {
            logger.warn("No recipients configured for alert emails");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipients.toArray(new String[0]));
            message.setSubject("⚠️ ALERT: Blockchain Integrity Compromised");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String emailContent = "SECURITY ALERT: Blockchain Integrity Check Failed\n\n" +
                    "Timestamp: " + timestamp + "\n" +
                    "Status: TAMPERED\n\n" +
                    "The blockchain integrity verification has failed. This could indicate unauthorized " +
                    "modification or tampering with the blockchain data.\n\n" +
                    "Additional Information:\n" + additionalInfo + "\n\n" +
                    "Immediate action is recommended.\n\n" +
                    "This is an automated alert. Please do not reply to this email.";
            
            message.setText(emailContent);
            
            mailSender.send(message);
            logger.info("Integrity alert email sent to {} recipients", recipients.size());
        } catch (Exception e) {
            logger.error("Failed to send integrity alert email: {}", e.getMessage(), e);
        }
    }
    
    // Optional: Method to send test email
    public boolean sendTestEmail(String recipient) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipient);
            message.setSubject("Test Email from Blockchain Integrity Service");
            message.setText("This is a test email from the Blockchain Integrity Monitoring Service. " +
                    "If you received this email, you are properly configured to receive alerts.");
            
            mailSender.send(message);
            logger.info("Test email sent to {}", recipient);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send test email: {}", e.getMessage(), e);
            return false;
        }
    }
} 