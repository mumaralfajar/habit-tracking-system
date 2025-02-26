package com.habitsystem.notification.service;

import com.habitsystem.notification.template.EmailTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailTemplates emailTemplates;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String username, String verificationToken) {
        String verificationLink = String.format("%s/verify-email?token=%s", 
            frontendUrl, verificationToken);
            
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(EmailTemplates.Subject.WELCOME_VERIFICATION);
        message.setText(emailTemplates.getWelcomeTemplate(username, verificationLink));
        
        mailSender.send(message);
        log.info("Verification email sent to: {}", to);
    }

    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(EmailTemplates.Subject.PASSWORD_RESET);
        message.setText(emailTemplates.getPasswordResetTemplate(username, resetLink));
        
        mailSender.send(message);
        log.info("Password reset email sent to: {}", to);
    }

    public void sendHabitReminder(String to, String username, String habitName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(EmailTemplates.Subject.HABIT_REMINDER);
        message.setText(emailTemplates.getHabitReminderTemplate(username, habitName));
        
        mailSender.send(message);
        log.info("Habit reminder email sent to: {}", to);
    }

    public void sendAchievementNotification(String to, String username, String achievement) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(EmailTemplates.Subject.ACHIEVEMENT_NOTIFICATION);
        message.setText(emailTemplates.getAchievementTemplate(username, achievement));
        
        mailSender.send(message);
        log.info("Achievement notification email sent to: {}", to);
    }
}
