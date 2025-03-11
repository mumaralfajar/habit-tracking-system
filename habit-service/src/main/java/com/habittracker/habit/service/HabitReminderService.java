package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitReminder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface HabitReminderService {
    
    List<HabitReminder> getRemindersByHabitId(Long habitId);
    
    HabitReminder createReminder(HabitReminder reminder);
    
    HabitReminder updateReminder(Long id, HabitReminder reminderDetails);
    
    void deleteReminder(Long id);
    
    void toggleReminderStatus(Long id, boolean enabled);
    
    List<HabitReminder> getActiveRemindersByUserId(String userId);
    
    List<HabitReminder> getActiveRemindersDueWithinTimeRange(LocalTime startTime, LocalTime endTime);
    
    List<HabitReminder> getActiveRemindersByUserIdAndDayOfWeek(String userId, String dayOfWeek);
    
    List<HabitReminder> getActiveRemindersByUserIdAndNotificationType(String userId, String notificationType);
}
