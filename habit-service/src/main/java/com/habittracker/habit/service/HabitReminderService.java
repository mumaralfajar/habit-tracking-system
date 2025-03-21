package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitReminder;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface HabitReminderService {
    
    List<HabitReminder> getRemindersByHabitId(UUID habitId);
    
    HabitReminder createReminder(HabitReminder reminder);
    
    HabitReminder updateReminder(UUID id, HabitReminder reminderDetails);
    
    void deleteReminder(UUID id);
    
    void toggleReminderStatus(UUID id, boolean enabled);
    
    List<HabitReminder> getActiveRemindersByUserId(UUID userId);
    
    List<HabitReminder> getActiveRemindersDueWithinTimeRange(LocalTime startTime, LocalTime endTime);
    
    List<HabitReminder> getActiveRemindersByUserIdAndDayOfWeek(UUID userId, String dayOfWeek);
    
    List<HabitReminder> getActiveRemindersByUserIdAndNotificationType(UUID userId, String notificationType);
}
