package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitStreak;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HabitStreakService {
    
    Optional<HabitStreak> getStreakByHabitId(Long habitId);
    
    List<HabitStreak> getStreaksByUserId(String userId);
    
    List<HabitStreak> getTopStreaksByUserId(String userId, int limit);
    
    void resetExpiredStreaks(LocalDateTime currentTime);
    
    Map<String, Object> getStreakStatistics(String userId);
    
    List<HabitStreak> getRecentlyBrokenStreaks(String userId, LocalDateTime since);
    
    List<HabitStreak> getStreaksByEfficiency(String userId);
}
