package com.habittracker.habit.mapper;

import com.habittracker.habit.dto.HabitResponseDTO;
import com.habittracker.habit.dto.HabitUpdateDTO;
import com.habittracker.habit.model.Habit;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class HabitMapper {

    /**
     * Converts a Habit entity to a HabitResponseDTO
     */
    public HabitResponseDTO toResponseDTO(Habit habit) {
        if (habit == null) {
            return null;
        }

        HabitResponseDTO responseDTO = new HabitResponseDTO();
        responseDTO.setId(habit.getId().toString());
        responseDTO.setUserId(habit.getUserId().toString());
        responseDTO.setName(habit.getName());
        responseDTO.setDescription(habit.getDescription());
        responseDTO.setFrequency(habit.getFrequency());
        responseDTO.setSchedule(habit.getSchedule());
        responseDTO.setPriority(habit.getPriority());
        responseDTO.setColor(habit.getColor());
        responseDTO.setIcon(habit.getIcon());
        responseDTO.setCreatedAt(habit.getCreatedAt());
        responseDTO.setUpdatedAt(habit.getUpdatedAt());
        
        if (habit.getCategory() != null) {
            responseDTO.setCategoryId(habit.getCategory().getId().toString());
            responseDTO.setCategoryName(habit.getCategory().getName());
        }
        
        if (habit.getStreak() != null) {
            responseDTO.setCurrentStreak(habit.getStreak().getCurrentStreak());
            responseDTO.setBestStreak(habit.getStreak().getBestStreak());
            responseDTO.setCompletionRate(habit.getStreak().getCompletionRate());
            responseDTO.setNextDueAt(habit.getStreak().getNextDueAt());
        }
        
        return responseDTO;
    }
    
    /**
     * Updates a Habit entity from a HabitUpdateDTO
     * Note: This doesn't save the entity, just updates its fields
     */
    public void updateHabitFromDTO(HabitUpdateDTO updateDTO, Habit habit) {
        if (updateDTO == null || habit == null) {
            return;
        }
        
        // Update the habit with fields from the DTO
        habit.setName(updateDTO.getName());
        habit.setFrequency(updateDTO.getFrequency());
        
        // Only set fields if they're provided in the DTO
        if (updateDTO.getDescription() != null) {
            habit.setDescription(updateDTO.getDescription());
        }
        
        if (updateDTO.getPriority() != null) {
            habit.setPriority(updateDTO.getPriority());
        }
        
        if (updateDTO.getSchedule() != null) {
            habit.setSchedule(updateDTO.getSchedule());
        }
        
        if (updateDTO.getColor() != null) {
            habit.setColor(updateDTO.getColor());
        }
        
        if (updateDTO.getIcon() != null) {
            habit.setIcon(updateDTO.getIcon());
        }
        
        if (updateDTO.getTimeOfDay() != null) {
            habit.setTimeOfDay(updateDTO.getTimeOfDay());
        }
        
        // Update the timestamp
        habit.setUpdatedAt(LocalDateTime.now());
    }
}
