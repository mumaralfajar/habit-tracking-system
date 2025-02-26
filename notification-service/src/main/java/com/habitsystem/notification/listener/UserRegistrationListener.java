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
    
    @KafkaListener(topics = "user-registered", groupId = "notification-service")
    public void handleUserRegistration(UserRegisteredEvent event) {
        log.info("Received user registration event for user: {}", event.getUsername());
        try {
            emailService.sendVerificationEmail(
                event.getEmail(), 
                event.getUsername(),
                event.getVerificationToken()
            );
            log.info("Verification email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", 
                event.getEmail(), e.getMessage(), e);
        }
    }
}
