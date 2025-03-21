package com.habittracker.habit.service;

import com.habittracker.habit.model.Habit;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ScheduleValidationService {
    
    boolean isHabitOnSchedule(Habit habit, LocalDateTime completionTime);
    
    LocalDateTime calculateNextDueDate(Habit habit, LocalDateTime referenceTime);
    
    boolean isHabitDue(Habit habit, LocalDateTime currentTime);
    
    int calculateRequiredCompletionsForPeriod(Habit habit, LocalDateTime start, LocalDateTime end);
    
    boolean hasMetFrequencyRequirements(UUID habitId, LocalDateTime start, LocalDateTime end);
}
