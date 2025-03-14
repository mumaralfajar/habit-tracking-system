package com.habittracker.habit.service;

import com.habittracker.habit.dto.DeleteResponseDTO;
import com.habittracker.habit.dto.HabitRequestDTO;
import com.habittracker.habit.dto.HabitResponseDTO;
import com.habittracker.habit.dto.HabitUpdateDTO;
import com.habittracker.habit.dto.PagedResponseDTO;
import com.habittracker.habit.model.Habit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HabitService {
    List<Habit> getAllHabitsByUserId(String userId);
    
    List<Habit> getHabitsByUserIdAndCategory(String userId, String categoryId);
    
    Optional<Habit> getHabitById(String id);
    
    HabitResponseDTO createHabit(HabitRequestDTO habitRequestDTO);
    
    // Existing method retained for backward compatibility
    // Habit updateHabit(String id, Habit habitDetails);
    
    // New method for updating with DTO and returning DTO
    HabitResponseDTO updateHabit(String id, HabitUpdateDTO habitUpdateDTO);
    
    DeleteResponseDTO deleteHabit(String id);
    
    List<Habit> getDueHabits(String userId, LocalDateTime currentTime);
    
    List<Habit> getHabitsByFrequency(String userId, String frequency);
    
    List<Habit> getRecentlyCreatedHabits(String userId, LocalDateTime since);
    
    List<Habit> getHighPriorityHabits(String userId, Integer minPriority);
    
    List<Habit> getUnusedHabits(String userId);
    
    // New methods for getting habit data with DTOs
    HabitResponseDTO getHabitResponseById(String id);
    
    PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdWithPagination(
            String userId, int page, int size, String sortBy, String sortDirection);
    
    PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdAndCategoryWithPagination(
            String userId, String categoryId, int page, int size, String sortBy, String sortDirection);
    
    List<HabitResponseDTO> getHighPriorityHabitsResponse(String userId, Integer minPriority);
    
    List<HabitResponseDTO> getDueHabitsResponse(String userId);
}
