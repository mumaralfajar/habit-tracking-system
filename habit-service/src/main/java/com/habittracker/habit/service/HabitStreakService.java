package com.habittracker.habit.service;

import com.habittracker.habit.model.HabitStreak;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface HabitStreakService {
    
    Optional<HabitStreak> getStreakByHabitId(UUID habitId);
    
    List<HabitStreak> getStreaksByUserId(UUID userId);
    
    List<HabitStreak> getTopStreaksByUserId(UUID userId, int limit);
    
    void resetExpiredStreaks(LocalDateTime currentTime);
    
    Map<String, Object> getStreakStatistics(UUID userId);
    
    List<HabitStreak> getRecentlyBrokenStreaks(UUID userId, LocalDateTime since);
    
    List<HabitStreak> getStreaksByEfficiency(UUID userId);
}
