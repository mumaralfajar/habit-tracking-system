package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitReminder;

import java.util.List;

public interface ReminderNotificationService {
    
    void sendReminders(List<HabitReminder> reminders);
    
    void sendReminderNotification(HabitReminder reminder);
    
    void sendDueHabitNotifications(String userId);
    
    void processPendingReminders();
}
