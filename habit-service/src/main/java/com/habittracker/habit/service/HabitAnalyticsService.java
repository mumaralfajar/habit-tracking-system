package com.habittracker.habit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HabitAnalyticsService {
    
    Map<String, Object> getUserStats(UUID userId);
    
    Map<String, Object> getHabitStats(UUID habitId);
    
    Map<String, Object> getUserCompletionTrends(UUID userId, LocalDateTime start, LocalDateTime end);
    
    List<Map<String, Object>> getTopPerformingHabits(UUID userId, int limit);
    
    List<Map<String, Object>> getHabitsNeedingAttention(UUID userId, int limit);
    
    Map<String, Object> getCategoryDistribution(UUID userId);
    
    Map<String, Object> getStreakAnalysis(UUID userId);
    
    Map<String, Object> getTimeOfDayAnalysis(UUID userId);
    
    Map<String, Double> getPredictedCompletionRates(UUID userId);
}
