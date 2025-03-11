package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.model.HabitTrackingRecord;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.repository.HabitTrackingRecordRepository;
import com.habittracker.habit.service.HabitTrackingService;
import com.habittracker.habit.service.ScheduleValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitTrackingServiceImpl implements HabitTrackingService {

    private final HabitRepository habitRepository;
    private final HabitTrackingRecordRepository trackingRecordRepository;
    private final HabitStreakRepository habitStreakRepository;
    private final ScheduleValidationService scheduleValidationService;
    
    @Override
    public HabitTrackingRecord trackHabitCompletion(Long habitId, HabitTrackingRecord record) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit not found with id: " + habitId));
        
        record.setHabit(habit);
        
        if (record.getCompletedAt() == null) {
            record.setCompletedAt(LocalDateTime.now());
        }
        
        // Save the tracking record
        HabitTrackingRecord savedRecord = trackingRecordRepository.save(record);
        
        // Update streak information
        updateHabitStreak(habit, record.getCompletedAt());
        
        return savedRecord;
    }
    
    @Override
    public List<HabitTrackingRecord> getTrackingRecordsByHabitId(Long habitId) {
        return trackingRecordRepository.findByHabitId(habitId);
    }
    
    @Override
    public List<HabitTrackingRecord> getTrackingRecordsByDateRange(Long habitId, LocalDateTime start, LocalDateTime end) {
        return trackingRecordRepository.findByHabitIdAndCompletedAtBetween(habitId, start, end);
    }
    
    @Override
    public List<HabitTrackingRecord> getUserTrackingRecordsByDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return trackingRecordRepository.findByUserIdAndCompletedAtBetween(userId, start, end);
    }
    
    @Override
    public Optional<HabitTrackingRecord> getMostRecentCompletion(Long habitId) {
        return trackingRecordRepository.findMostRecentByHabitId(habitId);
    }
    
    @Override
    public Map<Long, Integer> getHabitCompletionCountsForUser(String userId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = trackingRecordRepository.getCompletionStatsByUserIdBetweenDates(userId, start, end);
        Map<Long, Integer> completionCounts = new HashMap<>();
        
        for (Object[] row : results) {
            Long habitId = (Long) row[0];
            Long count = (Long) row[1];
            completionCounts.put(habitId, count.intValue());
        }
        
        return completionCounts;
    }
    
    @Override
    public Long countCompletionsInRange(Long habitId, LocalDateTime start, LocalDateTime end) {
        return trackingRecordRepository.countCompletionsInRange(habitId, start, end);
    }
    
    @Override
    public Map<Long, Double> getHabitDifficultyRatings(String userId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = trackingRecordRepository.getCompletionStatsByUserIdBetweenDates(userId, start, end);
        Map<Long, Double> difficultyRatings = new HashMap<>();
        
        for (Object[] row : results) {
            Long habitId = (Long) row[0];
            Double avgDifficulty = (Double) row[2];
            difficultyRatings.put(habitId, avgDifficulty);
        }
        
        return difficultyRatings;
    }
    
    private void updateHabitStreak(Habit habit, LocalDateTime completionTime) {
        HabitStreak streak = habit.getStreak();
        
        if (streak == null) {
            streak = HabitStreak.builder()
                    .habit(habit)
                    .currentStreak(0)
                    .bestStreak(0)
                    .completionRate(0.0)
                    .build();
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isOnTime = scheduleValidationService.isHabitOnSchedule(habit, completionTime);
        
        // If completed on time, increment streak
        if (isOnTime) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            
            // Update best streak if needed
            if (streak.getCurrentStreak() > streak.getBestStreak()) {
                streak.setBestStreak(streak.getCurrentStreak());
            }
        } else {
            // Reset streak if completed late
            streak.setCurrentStreak(1);
        }
        
        // Update last completed time
        streak.setLastCompletedAt(completionTime);
        
        // Calculate next due date
        streak.setNextDueAt(scheduleValidationService.calculateNextDueDate(habit, completionTime));
        
        // Calculate completion rate
        updateCompletionRate(habit, streak);
        
        habitStreakRepository.save(streak);
    }
    
    private void updateCompletionRate(Habit habit, HabitStreak streak) {
        LocalDateTime startOfTracking = habit.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate expected completions for the period since habit creation
        int totalExpectedCompletions = scheduleValidationService
                .calculateRequiredCompletionsForPeriod(habit, startOfTracking, now);
        
        // Get actual completions
        long actualCompletions = trackingRecordRepository.countCompletionsInRange(
                habit.getId(), startOfTracking, now);
        
        // Calculate and set completion rate
        double completionRate = (totalExpectedCompletions > 0)
                ? (double) actualCompletions / totalExpectedCompletions 
                : 0;
        
        streak.setCompletionRate(completionRate);
    }
}
