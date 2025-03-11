package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.service.HabitStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitStreakServiceImpl implements HabitStreakService {

    private final HabitStreakRepository streakRepository;
    
    @Override
    public Optional<HabitStreak> getStreakByHabitId(Long habitId) {
        return streakRepository.findByHabitId(habitId);
    }
    
    @Override
    public List<HabitStreak> getStreaksByUserId(String userId) {
        return streakRepository.findByUserId(userId);
    }
    
    @Override
    public List<HabitStreak> getTopStreaksByUserId(String userId, int limit) {
        return streakRepository.findByUserIdOrderByCurrentStreakDesc(userId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void scheduledResetExpiredStreaks() {
        resetExpiredStreaks(LocalDateTime.now());
    }
    
    @Override
    @Transactional
    public void resetExpiredStreaks(LocalDateTime currentTime) {
        if (currentTime == null) {
            currentTime = LocalDateTime.now();
        }
        
        List<HabitStreak> expiredStreaks = streakRepository.findStreaksToReset(currentTime);
        
        for (HabitStreak streak : expiredStreaks) {
            // Only reset if the streak was active
            if (streak.getCurrentStreak() > 0) {
                streak.setCurrentStreak(0);
                streakRepository.save(streak);
            }
        }
    }
    
    @Override
    public Map<String, Object> getStreakStatistics(String userId) {
        List<HabitStreak> userStreaks = streakRepository.findByUserId(userId);
        
        int totalCurrentStreak = userStreaks.stream()
                .mapToInt(HabitStreak::getCurrentStreak)
                .sum();
                
        int maxCurrentStreak = userStreaks.stream()
                .mapToInt(HabitStreak::getCurrentStreak)
                .max()
                .orElse(0);
                
        int maxEverStreak = userStreaks.stream()
                .mapToInt(HabitStreak::getBestStreak)
                .max()
                .orElse(0);
                
        double avgCompletionRate = userStreaks.stream()
                .mapToDouble(HabitStreak::getCompletionRate)
                .average()
                .orElse(0.0);
        
        long activeHabits = userStreaks.stream()
                .filter(streak -> streak.getCurrentStreak() > 0)
                .count();
                
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCurrentStreak", totalCurrentStreak);
        stats.put("maxCurrentStreak", maxCurrentStreak);
        stats.put("maxEverStreak", maxEverStreak);
        stats.put("avgCompletionRate", avgCompletionRate);
        stats.put("activeHabits", activeHabits);
        
        return stats;
    }
    
    @Override
    public List<HabitStreak> getRecentlyBrokenStreaks(String userId, LocalDateTime since) {
        return streakRepository.findRecentlyBrokenStreaks(userId, since);
    }
    
    @Override
    public List<HabitStreak> getStreaksByEfficiency(String userId) {
        return streakRepository.findStreaksByEfficiency(userId);
    }
}
