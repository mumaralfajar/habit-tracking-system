package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.service.HabitService;
import com.habittracker.habit.service.ScheduleValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitStreakRepository habitStreakRepository;
    private final ScheduleValidationService scheduleValidationService;
    
    @Override
    public List<Habit> getAllHabitsByUserId(String userId) {
        return habitRepository.findAllByUserId(userId);
    }
    
    @Override
    public List<Habit> getHabitsByUserIdAndCategory(String userId, String categoryId) {
        return habitRepository.findAllByUserIdAndCategoryId(userId, categoryId);
    }
    
    @Override
    public Optional<Habit> getHabitById(String id) {
        return habitRepository.findById(id);
    }
    
    @Override
    public Habit createHabit(Habit habit) {
        Habit savedHabit = habitRepository.save(habit);
        
        // Initialize streak data
        HabitStreak streak = HabitStreak.builder()
                .habit(savedHabit)
                .currentStreak(0)
                .bestStreak(0)
                .completionRate(0.0)
                .nextDueAt(scheduleValidationService.calculateNextDueDate(savedHabit, LocalDateTime.now()))
                .build();
        
        habitStreakRepository.save(streak);
        savedHabit.setStreak(streak);
        
        return savedHabit;
    }
    
    @Override
    public Habit updateHabit(String id, Habit habitDetails) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habit not found with id: " + id));
        
        // Update basic fields
        habit.setName(habitDetails.getName());
        habit.setDescription(habitDetails.getDescription());
        habit.setFrequency(habitDetails.getFrequency());
        habit.setSchedule(habitDetails.getSchedule());
        habit.setTimeOfDay(habitDetails.getTimeOfDay());
        habit.setPriority(habitDetails.getPriority());
        habit.setColor(habitDetails.getColor());
        habit.setIcon(habitDetails.getIcon());
        habit.setCategory(habitDetails.getCategory());
        
        // Recalculate next due date if frequency or schedule changes
        if (!habit.getFrequency().equals(habitDetails.getFrequency()) || 
                (habit.getSchedule() != null && !habit.getSchedule().equals(habitDetails.getSchedule()))) {
            
            HabitStreak streak = habit.getStreak();
            streak.setNextDueAt(scheduleValidationService.calculateNextDueDate(habitDetails, LocalDateTime.now()));
            habitStreakRepository.save(streak);
        }
        
        return habitRepository.save(habit);
    }
    
    @Override
    public void deleteHabit(String id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habit not found with id: " + id));
        
        habitRepository.delete(habit);
    }
    
    @Override
    public List<Habit> getDueHabits(String userId, LocalDateTime currentTime) {
        return habitRepository.findDueHabits(userId, currentTime);
    }
    
    @Override
    public List<Habit> getHabitsByFrequency(String userId, String frequency) {
        return habitRepository.findByUserIdAndFrequency(userId, frequency);
    }
    
    @Override
    public List<Habit> getRecentlyCreatedHabits(String userId, LocalDateTime since) {
        return habitRepository.findByUserIdAndCreatedAtBetween(userId, since, LocalDateTime.now());
    }
    
    @Override
    public List<Habit> getHighPriorityHabits(String userId, Integer minPriority) {
        return habitRepository.findByUserIdAndMinimumPriority(userId, minPriority);
    }
    
    @Override
    public List<Habit> getUnusedHabits(String userId) {
        return habitRepository.findUnusedHabitsByUserId(userId);
    }
}
