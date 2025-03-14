package com.habittracker.habit.service.impl;

import com.habittracker.habit.dto.DeleteResponseDTO;
import com.habittracker.habit.dto.HabitRequestDTO;
import com.habittracker.habit.dto.HabitResponseDTO;
import com.habittracker.habit.dto.HabitUpdateDTO;
import com.habittracker.habit.dto.PagedResponseDTO;
import com.habittracker.habit.exception.InvalidRequestException;
import com.habittracker.habit.exception.ResourceNotFoundException;
import com.habittracker.habit.mapper.HabitMapper;
import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.repository.CategoryRepository;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.service.HabitService;
import com.habittracker.habit.service.ScheduleValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HabitServiceImpl implements HabitService {
    private final HabitRepository habitRepository;
    private final HabitStreakRepository habitStreakRepository;
    private final CategoryRepository categoryRepository;
    private final ScheduleValidationService scheduleValidationService;
    private final HabitMapper habitMapper;

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
    @Transactional
    public HabitResponseDTO createHabit(HabitRequestDTO dto) {
        // Create a new habit entity
        Habit habit = new Habit();
        
        try {
            // Set required fields
            habit.setId(UUID.randomUUID());
            habit.setUserId(UUID.fromString(dto.getUserId()));
            habit.setName(dto.getName());
            habit.setFrequency(dto.getFrequency());
            
            // Set optional fields
            habit.setDescription(dto.getDescription());
            habit.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
            habit.setSchedule(dto.getSchedule());
            habit.setColor(dto.getColor());
            habit.setIcon(dto.getIcon());
            habit.setCreatedAt(LocalDateTime.now());
            
            // Associate with category if provided
            if (dto.getCategoryId() != null && !dto.getCategoryId().isEmpty()) {
                categoryRepository.findById(dto.getCategoryId())
                    .ifPresentOrElse(
                        habit::setCategory,
                        () -> {
                            throw new ResourceNotFoundException("Category", "id", dto.getCategoryId());
                        }
                    );
            }
            
            // Save the habit
            Habit savedHabit = habitRepository.save(habit);
            
            // Initialize streak
            HabitStreak streak = new HabitStreak();
            streak.setHabitId(savedHabit.getId());
            streak.setHabit(savedHabit);
            streak.setCurrentStreak(0);
            streak.setBestStreak(0);
            streak.setCompletionRate(0.0);
            
            // Calculate next due date
            LocalDateTime nextDueAt = scheduleValidationService.calculateNextDueDate(savedHabit, LocalDateTime.now());
            streak.setNextDueAt(nextDueAt);
            
            habitStreakRepository.save(streak);
            savedHabit.setStreak(streak);
            
            // Convert to response DTO
            return habitMapper.toResponseDTO(savedHabit);
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID or other formatting issues
            throw new InvalidRequestException("Invalid request data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other unexpected errors
            if (!(e instanceof ResourceNotFoundException)) {
                throw new InvalidRequestException("Error creating habit: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HabitResponseDTO getHabitResponseById(String id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", "id", id));
        
        return habitMapper.toResponseDTO(habit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdWithPagination(
            String userId, int page, int size, String sortBy, String sortDirection) {
        
        // Validate input parameters
        validatePaginationParams(page, size, sortBy);
        
        // Create Sort object based on sort direction
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        // Create pageable instance
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get page of habits
        Page<Habit> habitPage = habitRepository.findAllByUserIdPaged(userId, pageable);
        
        // Map to response DTOs
        List<HabitResponseDTO> habitDTOs = habitPage.getContent().stream()
                .map(habitMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        // Build response
        return buildPagedResponse(habitDTOs, habitPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<HabitResponseDTO> getHabitsByUserIdAndCategoryWithPagination(
            String userId, String categoryId, int page, int size, String sortBy, String sortDirection) {
        
        // Validate input parameters
        validatePaginationParams(page, size, sortBy);
        
        // Check if category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        // Create Sort object based on sort direction
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        // Create pageable instance
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get page of habits
        Page<Habit> habitPage = habitRepository.findAllByUserIdAndCategoryIdPaged(userId, categoryId, pageable);
        
        // Map to response DTOs
        List<HabitResponseDTO> habitDTOs = habitPage.getContent().stream()
                .map(habitMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        // Build response
        return buildPagedResponse(habitDTOs, habitPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HabitResponseDTO> getHighPriorityHabitsResponse(String userId, Integer minPriority) {
        List<Habit> habits = getHighPriorityHabits(userId, minPriority);
        
        return habits.stream()
                .map(habitMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HabitResponseDTO> getDueHabitsResponse(String userId) {
        List<Habit> habits = getDueHabits(userId, LocalDateTime.now());
        
        return habits.stream()
                .map(habitMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to validate pagination parameters
     */
    private void validatePaginationParams(int page, int size, String sortBy) {
        if (page < 0) {
            throw new InvalidRequestException("Page number cannot be less than zero");
        }
        
        if (size <= 0) {
            throw new InvalidRequestException("Page size must be greater than zero");
        }
        
        if (size > 100) {
            throw new InvalidRequestException("Page size must not be greater than 100");
        }
        
        // Add validation for allowed sort fields if needed
        List<String> allowedSortFields = List.of("name", "createdAt", "priority");
        if (!allowedSortFields.contains(sortBy)) {
            throw new InvalidRequestException("Sort parameter must be one of: " + String.join(", ", allowedSortFields));
        }
    }
    
    /**
     * Helper method to build the paged response
     */
    private <T> PagedResponseDTO<T> buildPagedResponse(List<T> content, Page<?> page) {
        return PagedResponseDTO.<T>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
    
    @Override
    @Transactional
    public HabitResponseDTO updateHabit(String id, HabitUpdateDTO updateDTO) {
        try {
            // Find the habit by ID
            Habit habit = habitRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Habit", "id", id));
            
            // Store original values for comparison
            String originalFrequency = habit.getFrequency();
            String originalSchedule = habit.getSchedule();
            
            // Update category if provided
            if (updateDTO.getCategoryId() != null) {
                if (updateDTO.getCategoryId().isEmpty()) {
                    // Clear the category if empty string is provided
                    habit.setCategory(null);
                } else {
                    // Set the new category
                    categoryRepository.findById(updateDTO.getCategoryId())
                        .ifPresentOrElse(
                            habit::setCategory,
                            () -> {
                                throw new ResourceNotFoundException("Category", "id", updateDTO.getCategoryId());
                            }
                        );
                }
            }
            
            // Update habit fields from DTO
            habitMapper.updateHabitFromDTO(updateDTO, habit);
            
            // Recalculate next due date if frequency or schedule changed
            boolean scheduleChanged = !originalFrequency.equals(habit.getFrequency()) || 
                    (originalSchedule != null && !originalSchedule.equals(habit.getSchedule()));
                    
            if (scheduleChanged && habit.getStreak() != null) {
                HabitStreak streak = habit.getStreak();
                streak.setNextDueAt(scheduleValidationService.calculateNextDueDate(habit, LocalDateTime.now()));
                habitStreakRepository.save(streak);
            }
            
            // Save the updated habit
            Habit updatedHabit = habitRepository.save(habit);
            
            // Return response DTO
            return habitMapper.toResponseDTO(updatedHabit);
            
        } catch (IllegalArgumentException e) {
            // Handle invalid input data
            throw new InvalidRequestException("Invalid request data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other unexpected errors
            if (!(e instanceof ResourceNotFoundException)) {
                throw new InvalidRequestException("Error updating habit: " + e.getMessage(), e);
            }
            throw e;
        }
    }
    
    @Override
    @Transactional
    public DeleteResponseDTO deleteHabit(String id) {
        try {
            // Find the habit to ensure it exists
            Habit habit = habitRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Habit", "id", id));
            
            String habitName = habit.getName(); // Store name before deletion for response
            
            // Delete the habit (cascading will be handled by JPA annotations)
            habitRepository.delete(habit);
            
            log.info("Habit deleted: ID={}, name={}", id, habitName);
            
            // Return success response
            return DeleteResponseDTO.builder()
                    .id(id)
                    .message("Habit '" + habitName + "' successfully deleted")
                    .timestamp(LocalDateTime.now())
                    .successful(true)
                    .build();
                    
        } catch (Exception e) {
            // If the exception is not ResourceNotFoundException, log it and wrap it
            if (!(e instanceof ResourceNotFoundException)) {
                log.error("Error deleting habit with ID: {}", id, e);
                throw new InvalidRequestException("Error deleting habit: " + e.getMessage(), e);
            }
            throw e;
        }
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
