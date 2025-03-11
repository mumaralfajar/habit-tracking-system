package com.habittracker.habit.service;

import com.habittracker.habit.model.Habit;

import java.time.LocalDateTime;

public interface ScheduleValidationService {
    
    boolean isHabitOnSchedule(Habit habit, LocalDateTime completionTime);
    
    LocalDateTime calculateNextDueDate(Habit habit, LocalDateTime referenceTime);
    
    boolean isHabitDue(Habit habit, LocalDateTime currentTime);
    
    int calculateRequiredCompletionsForPeriod(Habit habit, LocalDateTime start, LocalDateTime end);
    
    boolean hasMetFrequencyRequirements(Long habitId, LocalDateTime start, LocalDateTime end);
}
