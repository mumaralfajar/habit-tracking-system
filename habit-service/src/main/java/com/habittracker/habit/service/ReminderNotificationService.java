package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitReminder;

import java.util.List;
import java.util.UUID;

public interface ReminderNotificationService {
    
    void sendReminders(List<HabitReminder> reminders);
    
    void sendReminderNotification(HabitReminder reminder);
    
    void sendDueHabitNotifications(UUID userId);
    
    void processPendingReminders();
}
