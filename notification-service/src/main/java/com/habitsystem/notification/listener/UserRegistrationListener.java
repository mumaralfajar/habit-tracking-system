package com.habitsystem.notification.listener;

import com.habitsystem.notification.service.EmailService;
import com.habitsystem.notification.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {
    
    private final EmailService emailService;
    
    @KafkaListener(
        topics = "user-registered", 
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserRegistration(UserRegisteredEvent event) {
        log.info("Received registration event for user: {}", 
                event.getUsername() != null ? event.getUsername() : "unknown");
        
        try {
            processRegistrationEvent(event);
        } catch (Exception e) {
            log.error("Error processing registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process registration", e);
        }
    }
    
    private void processRegistrationEvent(UserRegisteredEvent event) {
        log.info("Processing registration event for user: {}", event.getUsername());
        
        if (event.getEmail() == null) {
            log.error("Invalid event - missing email address");
            return;
        }
        
        try {
            emailService.sendVerificationEmail(
                event.getEmail(), 
                event.getUsername(),
                event.getVerificationToken()
            );
            log.info("Verification email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", 
                event.getEmail(), e.getMessage());
            throw e;  // Rethrow to trigger retry mechanism
        }
    }
    
    @KafkaListener(
        topics = "user-registered-dlt", 
        groupId = "notification-service-dlt"
    )
    public void processDltMessages(UserRegisteredEvent event) {
        log.warn("Processing message from DLT: userId={}, username={}",
                event.getUserId(), event.getUsername());
        
        try {
            // Implement special handling for DLT messages
            // For example, store in database for manual review
            log.info("DLT message processed for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing DLT message: {}", e.getMessage(), e);
        }
    }
}
