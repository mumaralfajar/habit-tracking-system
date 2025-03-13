package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.model.HabitTrackingRecord;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.repository.HabitTrackingRecordRepository;
import com.habittracker.habit.service.HabitAnalyticsService;
import com.habittracker.habit.service.HabitStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitAnalyticsServiceImpl implements HabitAnalyticsService {

    private final HabitRepository habitRepository;
    private final HabitTrackingRecordRepository trackingRecordRepository;
    private final HabitStreakRepository streakRepository;
    private final HabitStreakService streakService;

    @Override
    public Map<String, Object> getUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        // Habit counts
        List<Habit> userHabits = habitRepository.findAllByUserId(userId);
        stats.put("totalHabits", userHabits.size());
        
        // Completion counts for different time periods
        List<HabitTrackingRecord> todayRecords = trackingRecordRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfToday, now);
        stats.put("completionsToday", todayRecords.size());
        
        List<HabitTrackingRecord> weekRecords = trackingRecordRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfWeek, now);
        stats.put("completionsThisWeek", weekRecords.size());
        
        List<HabitTrackingRecord> monthRecords = trackingRecordRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfMonth, now);
        stats.put("completionsThisMonth", monthRecords.size());
        
        // Streak information
        Map<String, Object> streakStats = streakService.getStreakStatistics(userId);
        stats.putAll(streakStats);
        
        return stats;
    }

    @Override
    public Map<String, Object> getHabitStats(String habitId) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        // Get habit details
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null) {
            return stats;
        }
        
        stats.put("habitId", habit.getId());
        stats.put("habitName", habit.getName());
        
        // Get streak information
        HabitStreak streak = habit.getStreak();
        if (streak != null) {
            stats.put("currentStreak", streak.getCurrentStreak());
            stats.put("bestStreak", streak.getBestStreak());
            stats.put("completionRate", streak.getCompletionRate());
            stats.put("lastCompletedAt", streak.getLastCompletedAt());
            stats.put("nextDueAt", streak.getNextDueAt());
        }
        
        // Recent completions
        List<HabitTrackingRecord> recentRecords = trackingRecordRepository
                .findByHabitIdAndCompletedAtBetween(habitId, thirtyDaysAgo, now);
        stats.put("completionsLast30Days", recentRecords.size());
        
        // Average ratings if available
        OptionalDouble avgDifficulty = recentRecords.stream()
                .filter(r -> r.getDifficultyRating() != null)
                .mapToInt(HabitTrackingRecord::getDifficultyRating)
                .average();
                
        OptionalDouble avgMood = recentRecords.stream()
                .filter(r -> r.getMoodRating() != null)
                .mapToInt(HabitTrackingRecord::getMoodRating)
                .average();
                
        stats.put("avgDifficulty", avgDifficulty.isPresent() ? avgDifficulty.getAsDouble() : null);
        stats.put("avgMood", avgMood.isPresent() ? avgMood.getAsDouble() : null);
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserCompletionTrends(String userId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> trends = new HashMap<>();
        
        List<HabitTrackingRecord> records = trackingRecordRepository
                .findByUserIdAndCompletedAtBetween(userId, start, end);
        
        // Group by day
        Map<LocalDate, Long> completionsByDay = records.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getCompletedAt().toLocalDate(),
                    Collectors.counting()
                ));
                
        // Format for output
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        Map<String, Long> formattedTrends = new HashMap<>();
        
        LocalDate current = start.toLocalDate();
        while (!current.isAfter(end.toLocalDate())) {
            String dateStr = current.format(formatter);
            formattedTrends.put(dateStr, completionsByDay.getOrDefault(current, 0L));
            current = current.plusDays(1);
        }
        
        trends.put("dailyCompletions", formattedTrends);
        
        // Calculate weekly averages
        Map<Integer, Double> weeklyAvg = new HashMap<>();
        records.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getCompletedAt().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()),
                    Collectors.averagingInt(record -> 1)
                ))
                .forEach((week, avg) -> weeklyAvg.put(week, avg * 7)); // Multiply by 7 to get weekly total
        
        trends.put("weeklyAverages", weeklyAvg);
        
        return trends;
    }

    @Override
    public List<Map<String, Object>> getTopPerformingHabits(String userId, int limit) {
        List<HabitStreak> topStreaks = streakRepository.findByUserIdOrderByCurrentStreakDesc(userId);
        
        return topStreaks.stream()
                .limit(limit)
                .map(streak -> {
                    Map<String, Object> habitInfo = new HashMap<>();
                    habitInfo.put("habitId", streak.getHabitId());
                    habitInfo.put("habitName", streak.getHabit().getName());
                    habitInfo.put("currentStreak", streak.getCurrentStreak());
                    habitInfo.put("bestStreak", streak.getBestStreak());
                    habitInfo.put("completionRate", streak.getCompletionRate());
                    return habitInfo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHabitsNeedingAttention(String userId, int limit) {
        // Get habits with low completion rates
        List<HabitStreak> streaks = streakRepository.findByCompletionRateRange(userId, 0.0, 0.5);
        
        return streaks.stream()
                .limit(limit)
                .map(streak -> {
                    Map<String, Object> habitInfo = new HashMap<>();
                    habitInfo.put("habitId", streak.getHabitId());
                    habitInfo.put("habitName", streak.getHabit().getName());
                    habitInfo.put("currentStreak", streak.getCurrentStreak());
                    habitInfo.put("completionRate", streak.getCompletionRate());
                    habitInfo.put("lastCompletedAt", streak.getLastCompletedAt());
                    return habitInfo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCategoryDistribution(String userId) {
        List<Habit> habits = habitRepository.findAllByUserId(userId);
        
        // Count habits by category
        Map<String, Long> distribution = habits.stream()
                .collect(Collectors.groupingBy(
                    habit -> habit.getCategory() != null ? habit.getCategory().getName() : "Uncategorized",
                    Collectors.counting()
                ));
                
        // Count completions by category in the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Map<String, Long> completionsByCategory = new HashMap<>();
        
        for (Habit habit : habits) {
            String categoryName = habit.getCategory() != null ? habit.getCategory().getName() : "Uncategorized";
            
            Long completions = trackingRecordRepository.countCompletionsInRange(
                    habit.getId().toString(), thirtyDaysAgo, LocalDateTime.now());
                    
            completionsByCategory.put(categoryName, 
                    completionsByCategory.getOrDefault(categoryName, 0L) + completions);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("habitCountByCategory", distribution);
        result.put("completionsByCategory", completionsByCategory);
        
        return result;
    }

    @Override
    public Map<String, Object> getStreakAnalysis(String userId) {
        List<HabitStreak> streaks = streakRepository.findByUserId(userId);
        
        // Distribution of streak lengths
        Map<Integer, Long> streakDistribution = streaks.stream()
                .collect(Collectors.groupingBy(
                    HabitStreak::getCurrentStreak,
                    Collectors.counting()
                ));
                
        // Longest current streaks
        List<Map<String, Object>> longestStreaks = streaks.stream()
                .sorted(Comparator.comparing(HabitStreak::getCurrentStreak).reversed())
                .limit(5)
                .map(streak -> {
                    Map<String, Object> streakInfo = new HashMap<>();
                    streakInfo.put("habitId", streak.getHabitId());
                    streakInfo.put("habitName", streak.getHabit().getName());
                    streakInfo.put("currentStreak", streak.getCurrentStreak());
                    return streakInfo;
                })
                .collect(Collectors.toList());
                
        // Average streak length
        double avgStreakLength = streaks.stream()
                .mapToInt(HabitStreak::getCurrentStreak)
                .average()
                .orElse(0);
                
        Map<String, Object> result = new HashMap<>();
        result.put("streakDistribution", streakDistribution);
        result.put("longestStreaks", longestStreaks);
        result.put("averageStreakLength", avgStreakLength);
        
        return result;
    }

    @Override
    public Map<String, Object> getTimeOfDayAnalysis(String userId) {
        // Get all tracking records for the user
        List<HabitTrackingRecord> records = trackingRecordRepository
                .findByUserIdAndCompletedAtBetween(
                        userId, 
                        LocalDateTime.now().minusMonths(3), 
                        LocalDateTime.now());
                        
        // Group by hour of day
        Map<Integer, Long> completionsByHour = records.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getCompletedAt().getHour(),
                    Collectors.counting()
                ));
        
        // Identify peak times
        Map.Entry<Integer, Long> peakHour = completionsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
                
        // Get more granular time periods
        Map<String, Long> completionsByTimePeriod = new HashMap<>();
        completionsByTimePeriod.put("morning", 
                countCompletionsInHourRange(records, 5, 11)); // 5am-11:59am
        completionsByTimePeriod.put("afternoon", 
                countCompletionsInHourRange(records, 12, 16)); // 12pm-4:59pm
        completionsByTimePeriod.put("evening", 
                countCompletionsInHourRange(records, 17, 20)); // 5pm-8:59pm
        completionsByTimePeriod.put("night", 
                countCompletionsInHourRange(records, 21, 4)); // 9pm-4:59am
                
        Map<String, Object> result = new HashMap<>();
        result.put("completionsByHour", completionsByHour);
        result.put("completionsByTimePeriod", completionsByTimePeriod);
        if (peakHour != null) {
            result.put("peakHour", peakHour.getKey());
            result.put("peakHourCount", peakHour.getValue());
        }
        
        return result;
    }

    @Override
    public Map<String, Double> getPredictedCompletionRates(String userId) {
        List<Habit> habits = habitRepository.findAllByUserId(userId);
        Map<String, Double> predictions = new HashMap<>();
        
        for (Habit habit : habits) {
            // Calculate trend based on recent history
            LocalDateTime now = LocalDateTime.now();
            
            // Last week's completion rate
            double lastWeekRate = calculateCompletionRateForPeriod(
                    habit.getId().toString(), now.minusDays(7), now);
                    
            // Week before that
            double previousWeekRate = calculateCompletionRateForPeriod(
                    habit.getId().toString(), now.minusDays(14), now.minusDays(7));
                    
            // Simple linear trend
            double trend = lastWeekRate - previousWeekRate;
            
            // Predicted rate (with bounds)
            double predictedRate = Math.min(Math.max(lastWeekRate + trend, 0.0), 1.0);
            
            predictions.put(habit.getName(), predictedRate);
        }
        
        return predictions;
    }
    
    // Helper method to count completions in a specific hour range
    private long countCompletionsInHourRange(List<HabitTrackingRecord> records, int startHour, int endHour) {
        if (startHour <= endHour) {
            return records.stream()
                    .filter(record -> {
                        int hour = record.getCompletedAt().getHour();
                        return hour >= startHour && hour <= endHour;
                    })
                    .count();
        } else {
            // Handle wraparound (e.g., 21 to 4)
            return records.stream()
                    .filter(record -> {
                        int hour = record.getCompletedAt().getHour();
                        return hour >= startHour || hour <= endHour;
                    })
                    .count();
        }
    }
    
    // Helper method to calculate completion rate for a specific period
    private double calculateCompletionRateForPeriod(String habitId, LocalDateTime start, LocalDateTime end) {
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null) {
            return 0.0;
        }
        
        // Count actual completions
        long actualCompletions = trackingRecordRepository.countCompletionsInRange(habitId, start, end);
        
        // Calculate expected completions based on frequency
        long expectedCompletions;
        
        switch (habit.getFrequency().toUpperCase()) {
            case "DAILY":
                expectedCompletions = ChronoUnit.DAYS.between(start, end);
                break;
            case "WEEKLY":
                expectedCompletions = ChronoUnit.WEEKS.between(start, end);
                break;
            case "MONTHLY":
                expectedCompletions = ChronoUnit.MONTHS.between(start, end);
                break;
            default:
                expectedCompletions = ChronoUnit.DAYS.between(start, end);
        }
        
        return expectedCompletions > 0 ? (double) actualCompletions / expectedCompletions : 0.0;
    }
}
