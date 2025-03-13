package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitReminder;

import java.time.LocalTime;
import java.util.List;

public interface HabitReminderService {
    
    List<HabitReminder> getRemindersByHabitId(String habitId);
    
    HabitReminder createReminder(HabitReminder reminder);
    
    HabitReminder updateReminder(String id, HabitReminder reminderDetails);
    
    void deleteReminder(String id);
    
    void toggleReminderStatus(String id, boolean enabled);
    
    List<HabitReminder> getActiveRemindersByUserId(String userId);
    
    List<HabitReminder> getActiveRemindersDueWithinTimeRange(LocalTime startTime, LocalTime endTime);
    
    List<HabitReminder> getActiveRemindersByUserIdAndDayOfWeek(String userId, String dayOfWeek);
    
    List<HabitReminder> getActiveRemindersByUserIdAndNotificationType(String userId, String notificationType);
}
