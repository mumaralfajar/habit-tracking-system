package com.habitsystem.notification.template;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

    public static class Subject {
        public static final String WELCOME_VERIFICATION = "Welcome to Habit Tracking System - Email Verification";
        public static final String PASSWORD_RESET = "Reset Your Password - Habit Tracking System";
        public static final String HABIT_REMINDER = "Daily Habit Reminder";
        public static final String ACHIEVEMENT_NOTIFICATION = "Congratulations on Your Achievement!";
    }

    public String getWelcomeTemplate(String username, String verificationLink) {
        return String.format("""
            Dear %s,
            
            Thank you for joining Habit Tracking System! We're excited to have you on board.
            
            Please verify your email address by clicking the link below:
            %s
            
            This verification link will expire in 24 hours.
            
            Key Features of Your Account:
            • Create and track daily habits
            • Set personal goals and milestones
            • Monitor your progress with detailed analytics
            • Receive personalized notifications and reminders
            
            Security Notice:
            • Keep your login credentials secure
            • Never share your password with anyone
            • Contact support immediately if you notice any suspicious activity
            
            If you have any questions or need assistance, please don't hesitate to contact our support team.
            
            Best regards,
            The Habit Tracking System Team
            
            Note: If you didn't create this account, please ignore this email.""",
            username, verificationLink
        );
    }

    public String getPasswordResetTemplate(String username, String resetLink) {
        return String.format("""
            Dear %s,
            
            We received a request to reset your password for your Habit Tracking System account.
            
            To reset your password, please click on the link below:
            %s
            
            This link will expire in 30 minutes for security reasons.
            
            If you didn't request this password reset, please ignore this email or contact support if you have concerns.
            
            Best regards,
            The Habit Tracking System Team
            
            Note: This is an automated message, please do not reply to this email.""",
            username, resetLink
        );
    }

    public String getHabitReminderTemplate(String username, String habitName) {
        return String.format("""
            Hi %s,
            
            This is a friendly reminder about your habit: %s
            
            Remember, consistency is key to building lasting habits! Take a moment to complete this habit and track your progress.
            
            Keep up the great work!
            
            Best regards,
            The Habit Tracking System Team""",
            username, habitName
        );
    }

    public String getAchievementTemplate(String username, String achievement) {
        return String.format("""
            Congratulations %s! 🎉
            
            You've reached an amazing milestone:
            %s
            
            This is a tremendous achievement and proves your dedication to self-improvement.
            Keep up the fantastic work - you're making great progress!
            
            Best regards,
            The Habit Tracking System Team""",
            username, achievement
        );
    }
}
