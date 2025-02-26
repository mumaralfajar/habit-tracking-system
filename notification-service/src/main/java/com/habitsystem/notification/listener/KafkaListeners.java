package com.habitsystem.notification.listener;

import com.habitsystem.notification.event.UserRegisteredEvent;
import com.habitsystem.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {

    private final EmailService emailService;

    @KafkaListener(
        topics = "user-registered",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received user registration event for user: {}", event.getUsername());
        try {
            emailService.sendVerificationEmail(
                event.getEmail(),
                event.getUsername(),
                event.getVerificationToken()
            );
            log.info("Verification email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage(), e);
        }
    }
}
