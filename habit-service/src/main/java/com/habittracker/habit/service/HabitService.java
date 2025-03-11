package com.habittracker.habit.service;

import com.habittracker.habit.model.Habit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HabitService {
    
    List<Habit> getAllHabitsByUserId(String userId);
    
    List<Habit> getHabitsByUserIdAndCategory(String userId, Long categoryId);
    
    Optional<Habit> getHabitById(Long id);
    
    Habit createHabit(Habit habit);
    
    Habit updateHabit(Long id, Habit habitDetails);
    
    void deleteHabit(Long id);
    
    List<Habit> getDueHabits(String userId, LocalDateTime currentTime);
    
    List<Habit> getHabitsByFrequency(String userId, String frequency);
    
    List<Habit> getRecentlyCreatedHabits(String userId, LocalDateTime since);
    
    List<Habit> getHighPriorityHabits(String userId, Integer minPriority);
    
    List<Habit> getUnusedHabits(String userId);
}
