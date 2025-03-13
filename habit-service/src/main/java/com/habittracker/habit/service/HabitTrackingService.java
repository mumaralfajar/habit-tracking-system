package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitTrackingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HabitTrackingService {
    
    HabitTrackingRecord trackHabitCompletion(String habitId, HabitTrackingRecord record);
    
    List<HabitTrackingRecord> getTrackingRecordsByHabitId(String habitId);
    
    List<HabitTrackingRecord> getTrackingRecordsByDateRange(String habitId, LocalDateTime start, LocalDateTime end);
    
    List<HabitTrackingRecord> getUserTrackingRecordsByDateRange(String userId, LocalDateTime start, LocalDateTime end);
    
    Optional<HabitTrackingRecord> getMostRecentCompletion(String habitId);
    
    Map<String, Integer> getHabitCompletionCountsForUser(String userId, LocalDateTime start, LocalDateTime end);
    
    Long countCompletionsInRange(String habitId, LocalDateTime start, LocalDateTime end);
    
    Map<String, Double> getHabitDifficultyRatings(String userId, LocalDateTime start, LocalDateTime end);
}
