package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitTrackingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface HabitTrackingService {
    
    HabitTrackingRecord trackHabitCompletion(UUID habitId, HabitTrackingRecord record);
    
    List<HabitTrackingRecord> getTrackingRecordsByHabitId(UUID habitId);
    
    List<HabitTrackingRecord> getTrackingRecordsByDateRange(UUID habitId, LocalDateTime start, LocalDateTime end);
    
    List<HabitTrackingRecord> getUserTrackingRecordsByDateRange(UUID userId, LocalDateTime start, LocalDateTime end);
    
    Optional<HabitTrackingRecord> getMostRecentCompletion(UUID habitId);
    
    Map<String, Integer> getHabitCompletionCountsForUser(UUID userId, LocalDateTime start, LocalDateTime end);
    
    Long countCompletionsInRange(UUID habitId, LocalDateTime start, LocalDateTime end);
    
    Map<String, Double> getHabitDifficultyRatings(UUID userId, LocalDateTime start, LocalDateTime end);
}
