package com.habittracker.habit.controller;

import com.habittracker.habit.dto.DeleteResponseDTO;
import com.habittracker.habit.dto.HabitRequestDTO;
import com.habittracker.habit.dto.HabitResponseDTO;
import com.habittracker.habit.dto.HabitUpdateDTO;
import com.habittracker.habit.dto.PagedResponseDTO;
import com.habittracker.habit.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    
    private final HabitService habitService;
    
    /**
     * Create a new habit
     */
    @PostMapping
    public ResponseEntity<HabitResponseDTO> createHabit(@Valid @RequestBody HabitRequestDTO habitRequestDTO) {
        HabitResponseDTO createdHabit = habitService.createHabit(habitRequestDTO);
        return new ResponseEntity<>(createdHabit, HttpStatus.CREATED);
    }
    
    /**
     * Get a single habit by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> getHabitById(@PathVariable String id) {
        HabitResponseDTO habit = habitService.getHabitResponseById(id);
        return ResponseEntity.ok(habit);
    }
    
    /**
     * Get all habits by user ID with pagination
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponseDTO<HabitResponseDTO>> getHabitsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        PagedResponseDTO<HabitResponseDTO> response;
        
        if (categoryId != null && !categoryId.isEmpty()) {
            response = habitService.getHabitsByUserIdAndCategoryWithPagination(
                    userId, categoryId, page, size, sortBy, sortDir);
        } else {
            response = habitService.getHabitsByUserIdWithPagination(
                    userId, page, size, sortBy, sortDir);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all high priority habits for a user
     */
    @GetMapping("/user/{userId}/priority")
    public ResponseEntity<List<HabitResponseDTO>> getHighPriorityHabits(
            @PathVariable String userId,
            @RequestParam(defaultValue = "3") Integer minPriority) {
        
        List<HabitResponseDTO> highPriorityHabits = habitService.getHighPriorityHabitsResponse(userId, minPriority);
        return ResponseEntity.ok(highPriorityHabits);
    }
    
    /**
     * Get all due habits for a user
     */
    @GetMapping("/user/{userId}/due")
    public ResponseEntity<List<HabitResponseDTO>> getDueHabits(@PathVariable String userId) {
        List<HabitResponseDTO> dueHabits = habitService.getDueHabitsResponse(userId);
        return ResponseEntity.ok(dueHabits);
    }
    
    /**
     * Update an existing habit by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> updateHabit(
            @PathVariable String id,
            @Valid @RequestBody HabitUpdateDTO habitUpdateDTO) {
        HabitResponseDTO updatedHabit = habitService.updateHabit(id, habitUpdateDTO);
        return ResponseEntity.ok(updatedHabit);
    }
    
    /**
     * Delete a habit by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponseDTO> deleteHabit(@PathVariable String id) {
        DeleteResponseDTO response = habitService.deleteHabit(id);
        return ResponseEntity.ok(response);
    }
}
