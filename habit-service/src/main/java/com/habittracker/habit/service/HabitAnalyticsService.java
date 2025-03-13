package com.habittracker.habit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HabitAnalyticsService {
    
    Map<String, Object> getUserStats(String userId);
    
    Map<String, Object> getHabitStats(String habitId);
    
    Map<String, Object> getUserCompletionTrends(String userId, LocalDateTime start, LocalDateTime end);
    
    List<Map<String, Object>> getTopPerformingHabits(String userId, int limit);
    
    List<Map<String, Object>> getHabitsNeedingAttention(String userId, int limit);
    
    Map<String, Object> getCategoryDistribution(String userId);
    
    Map<String, Object> getStreakAnalysis(String userId);
    
    Map<String, Object> getTimeOfDayAnalysis(String userId);
    
    Map<String, Double> getPredictedCompletionRates(String userId);
}
