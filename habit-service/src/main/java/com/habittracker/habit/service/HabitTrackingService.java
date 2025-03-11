package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitTrackingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HabitTrackingService {
    
    HabitTrackingRecord trackHabitCompletion(Long habitId, HabitTrackingRecord record);
    
    List<HabitTrackingRecord> getTrackingRecordsByHabitId(Long habitId);
    
    List<HabitTrackingRecord> getTrackingRecordsByDateRange(Long habitId, LocalDateTime start, LocalDateTime end);
    
    List<HabitTrackingRecord> getUserTrackingRecordsByDateRange(String userId, LocalDateTime start, LocalDateTime end);
    
    Optional<HabitTrackingRecord> getMostRecentCompletion(Long habitId);
    
    Map<Long, Integer> getHabitCompletionCountsForUser(String userId, LocalDateTime start, LocalDateTime end);
    
    Long countCompletionsInRange(Long habitId, LocalDateTime start, LocalDateTime end);
    
    Map<Long, Double> getHabitDifficultyRatings(String userId, LocalDateTime start, LocalDateTime end);
}
