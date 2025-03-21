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
import java.util.UUID;

public interface HabitService {
    List<Habit> getAllHabitsByUserId(UUID userId);
    
    List<Habit> getHabitsByUserIdAndCategory(UUID userId, UUID categoryId);
    
    Optional<Habit> getHabitById(UUID id);
    
    HabitResponseDTO createHabit(HabitRequestDTO habitRequestDTO);
    
    // Existing method retained for backward compatibility
    // Habit updateHabit(String id, Habit habitDetails);
    
    // New method for updating with DTO and returning DTO
    HabitResponseDTO updateHabit(UUID id, HabitUpdateDTO habitUpdateDTO);
    
    DeleteResponseDTO deleteHabit(UUID id);
    
    List<Habit> getDueHabits(UUID userId, LocalDateTime currentTime);
    
    List<Habit> getHabitsByFrequency(UUID userId, String frequency);
    
    List<Habit> getRecentlyCreatedHabits(UUID userId, LocalDateTime since);
    
    List<Habit> getHighPriorityHabits(UUID userId, Integer minPriority);
    
    List<Habit> getUnusedHabits(UUID userId);
    
    // New methods for getting habit data with DTOs
    HabitResponseDTO getHabitResponseById(UUID id);
    
    PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdWithPagination(
        UUID userId, int page, int size, String sortBy, String sortDirection);
    
    PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdAndCategoryWithPagination(
        UUID userId, UUID categoryId, int page, int size, String sortBy, String sortDirection);
    
    List<HabitResponseDTO> getHighPriorityHabitsResponse(UUID userId, Integer minPriority);
    
    List<HabitResponseDTO> getDueHabitsResponse(UUID userId);
}
