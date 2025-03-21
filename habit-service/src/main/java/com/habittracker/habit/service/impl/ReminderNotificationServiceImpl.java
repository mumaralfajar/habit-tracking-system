package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitReminder;
import com.habittracker.habit.repository.HabitReminderRepository;
import com.habittracker.habit.service.HabitService;
import com.habittracker.habit.service.ReminderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderNotificationServiceImpl implements ReminderNotificationService {

    private final HabitReminderRepository reminderRepository;
    private final HabitService habitService;
    private final KafkaTemplate<UUID, Object> kafkaTemplate;
    
    private static final String NOTIFICATION_TOPIC = "habit-notifications";
    
    @Override
    public void sendReminders(List<HabitReminder> reminders) {
        for (HabitReminder reminder : reminders) {
            sendReminderNotification(reminder);
        }
    }
    
    @Override
    public void sendReminderNotification(HabitReminder reminder) {
        // Skip disabled reminders
        if (!reminder.getEnabled()) {
            return;
        }
        
        // Create notification payload
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", reminder.getHabit().getUserId());
        notification.put("title", "Habit Reminder: " + reminder.getHabit().getName());
        notification.put("body", reminder.getMessage() != null ? reminder.getMessage() : "Time to complete your habit!");
        notification.put("type", reminder.getNotificationType());
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("habitId", reminder.getHabit().getId());
        
        // Send notification via Kafka
        try {
            kafkaTemplate.send(NOTIFICATION_TOPIC, reminder.getHabit().getUserId(), notification);
            log.info("Sent reminder notification for habit: {}", reminder.getHabit().getName());
        } catch (Exception e) {
            log.error("Failed to send reminder notification", e);
        }
    }
    
    @Override
    public void sendDueHabitNotifications(UUID userId) {
        List<Habit> dueHabits = habitService.getDueHabits(userId, LocalDateTime.now());
        
        for (Habit habit : dueHabits) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", userId);
            notification.put("title", "Habit Due: " + habit.getName());
            notification.put("body", "Your habit is due for completion!");
            notification.put("type", "PUSH");  // Default to push notification
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("habitId", habit.getId());
            
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC, userId, notification);
                log.info("Sent due habit notification for habit: {}", habit.getName());
            } catch (Exception e) {
                log.error("Failed to send due habit notification", e);
            }
        }
    }
    
    @Override
    @Scheduled(cron = "0 */15 * * * *") // Run every 15 minutes
    public void processPendingReminders() {
        log.info("Running scheduled reminder check");
        
        LocalTime now = LocalTime.now();
        LocalTime fifteenMinutesLater = now.plusMinutes(15);
        
        // Get current day of week
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        String dayOfWeekStr = today.toString();
        
        // Find reminders due in the next 15 minutes
        List<HabitReminder> dueReminders = reminderRepository
                .findActiveRemindersDueWithinTimeRange(now, fifteenMinutesLater)
                .stream()
                .filter(reminder -> {
                    // Check if reminder applies to current day
                    return reminder.getDaysOfWeek() == null || 
                           reminder.getDaysOfWeek().isEmpty() ||
                           reminder.getDaysOfWeek().contains(dayOfWeekStr);
                })
                .collect(Collectors.toList());
        
        if (!dueReminders.isEmpty()) {
            log.info("Found {} reminders to send", dueReminders.size());
            sendReminders(dueReminders);
        }
    }
}
