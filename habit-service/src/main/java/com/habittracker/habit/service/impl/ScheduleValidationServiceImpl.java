package com.habittracker.habit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.habit.model.Habit;
import com.habittracker.habit.repository.HabitTrackingRecordRepository;
import com.habittracker.habit.service.ScheduleValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleValidationServiceImpl implements ScheduleValidationService {

    private final HabitTrackingRecordRepository trackingRecordRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean isHabitOnSchedule(Habit habit, LocalDateTime completionTime) {
        LocalDateTime nextDue = habit.getStreak() != null ? habit.getStreak().getNextDueAt() : null;
        
        if (nextDue == null) {
            return true; // No schedule set, so any time is valid
        }
        
        return !completionTime.isAfter(nextDue);
    }
    
    @Override
    public LocalDateTime calculateNextDueDate(Habit habit, LocalDateTime referenceTime) {
        if (referenceTime == null) {
            referenceTime = LocalDateTime.now();
        }
        
        String frequency = habit.getFrequency().toUpperCase();
        String schedule = habit.getSchedule();
        
        switch (frequency) {
            case "DAILY":
                return referenceTime.plusDays(1);
                
            case "WEEKLY":
                return referenceTime.plusWeeks(1);
                
            case "MONTHLY":
                return referenceTime.plusMonths(1);
                
            case "YEARLY":
                return referenceTime.plusYears(1);
                
            case "X_TIMES_PER_WEEK":
                return calculateXTimesPerWeek(habit, referenceTime);
                
            case "X_TIMES_PER_MONTH":
                return calculateXTimesPerMonth(habit, referenceTime);
                
            case "SPECIFIC_DAYS":
                return calculateSpecificDays(habit, referenceTime);
                
            case "CUSTOM":
                return parseCustomSchedule(habit, referenceTime);
                
            default:
                log.warn("Unknown frequency type: {}, defaulting to daily", frequency);
                return referenceTime.plusDays(1);
        }
    }
    
    @Override
    public boolean isHabitDue(Habit habit, LocalDateTime currentTime) {
        if (habit.getStreak() == null || habit.getStreak().getNextDueAt() == null) {
            return false;
        }
        
        return !currentTime.isBefore(habit.getStreak().getNextDueAt());
    }
    
    @Override
    public int calculateRequiredCompletionsForPeriod(Habit habit, LocalDateTime start, LocalDateTime end) {
        String frequency = habit.getFrequency().toUpperCase();
        String schedule = habit.getSchedule();
        
        try {
            switch (frequency) {
                case "DAILY":
                    return (int) ChronoUnit.DAYS.between(start, end) + 1;
                    
                case "WEEKLY":
                    return (int) (ChronoUnit.DAYS.between(start, end) / 7) + 1;
                    
                case "MONTHLY":
                    return (int) ChronoUnit.MONTHS.between(start, end) + 1;
                    
                case "YEARLY":
                    return (int) ChronoUnit.YEARS.between(start, end) + 1;
                    
                case "X_TIMES_PER_WEEK":
                    JsonNode scheduleNode = objectMapper.readTree(schedule);
                    int timesPerWeek = scheduleNode.get("times").asInt();
                    return timesPerWeek * ((int) (ChronoUnit.DAYS.between(start, end) / 7) + 1);
                
                case "X_TIMES_PER_MONTH":
                    scheduleNode = objectMapper.readTree(schedule);
                    int timesPerMonth = scheduleNode.get("times").asInt();
                    return timesPerMonth * ((int) ChronoUnit.MONTHS.between(start, end) + 1);
                
                case "SPECIFIC_DAYS":
                    scheduleNode = objectMapper.readTree(schedule);
                    JsonNode daysNode = scheduleNode.get("days");
                    int daysCount = 0;
                    Set<DayOfWeek> daysOfWeek = new HashSet<>();
                    
                    for (JsonNode day : daysNode) {
                        daysOfWeek.add(DayOfWeek.valueOf(day.asText()));
                    }
                    
                    LocalDateTime current = start;
                    while (!current.isAfter(end)) {
                        if (daysOfWeek.contains(current.getDayOfWeek())) {
                            daysCount++;
                        }
                        current = current.plusDays(1);
                    }
                    return daysCount;
                    
                default:
                    return (int) ChronoUnit.DAYS.between(start, end) + 1;
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing schedule JSON", e);
            return (int) ChronoUnit.DAYS.between(start, end) + 1;
        }
    }
    
    @Override
    public boolean hasMetFrequencyRequirements(Long habitId, LocalDateTime start, LocalDateTime end) {
        Long completions = trackingRecordRepository.countCompletionsInRange(habitId, start, end);
        Habit habit = trackingRecordRepository.findByHabitId(habitId).get(0).getHabit();
        int required = calculateRequiredCompletionsForPeriod(habit, start, end);
        
        return completions >= required;
    }
    
    private LocalDateTime calculateXTimesPerWeek(Habit habit, LocalDateTime referenceTime) {
        try {
            JsonNode scheduleNode = objectMapper.readTree(habit.getSchedule());
            int timesPerWeek = scheduleNode.get("times").asInt();
            
            // Calculate completions this week
            LocalDateTime startOfWeek = referenceTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);
            
            long completionsThisWeek = trackingRecordRepository.countCompletionsInRange(
                    habit.getId(), startOfWeek, endOfWeek);
            
            if (completionsThisWeek < timesPerWeek) {
                // Not complete for this week yet, due tomorrow
                return referenceTime.plusDays(1);
            } else {
                // Complete for this week, due next Monday
                return endOfWeek.plusDays(1);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing X_TIMES_PER_WEEK schedule", e);
            return referenceTime.plusDays(1);
        }
    }
    
    private LocalDateTime calculateXTimesPerMonth(Habit habit, LocalDateTime referenceTime) {
        try {
            JsonNode scheduleNode = objectMapper.readTree(habit.getSchedule());
            int timesPerMonth = scheduleNode.get("times").asInt();
            
            // Calculate completions this month
            LocalDateTime startOfMonth = referenceTime.withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = referenceTime
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);
            
            long completionsThisMonth = trackingRecordRepository.countCompletionsInRange(
                    habit.getId(), startOfMonth, endOfMonth);
            
            if (completionsThisMonth < timesPerMonth) {
                // Not complete for this month yet, due tomorrow
                return referenceTime.plusDays(1);
            } else {
                // Complete for this month, due next month
                return endOfMonth.plusDays(1);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing X_TIMES_PER_MONTH schedule", e);
            return referenceTime.plusDays(1);
        }
    }
    
    private LocalDateTime calculateSpecificDays(Habit habit, LocalDateTime referenceTime) {
        try {
            JsonNode scheduleNode = objectMapper.readTree(habit.getSchedule());
            JsonNode daysNode = scheduleNode.get("days");
            
            Set<DayOfWeek> daysOfWeek = new HashSet<>();
            for (JsonNode day : daysNode) {
                daysOfWeek.add(DayOfWeek.valueOf(day.asText()));
            }
            
            // Find the next specified day
            LocalDateTime nextDate = referenceTime.plusDays(1);
            int maxIterations = 7; // Avoid infinite loop
            int iterations = 0;
            
            while (!daysOfWeek.contains(nextDate.getDayOfWeek()) && iterations < maxIterations) {
                nextDate = nextDate.plusDays(1);
                iterations++;
            }
            
            return nextDate;
        } catch (JsonProcessingException e) {
            log.error("Error parsing SPECIFIC_DAYS schedule", e);
            return referenceTime.plusDays(1);
        }
    }
    
    private LocalDateTime parseCustomSchedule(Habit habit, LocalDateTime referenceTime) {
        // For custom schedules, we would need to implement a more complex parser
        // For now, just return the next day as default
        log.warn("Custom schedule parsing not fully implemented, defaulting to daily");
        return referenceTime.plusDays(1);
    }
}
