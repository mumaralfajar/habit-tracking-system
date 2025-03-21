package com.habittracker.habit.service.impl;

import com.habittracker.habit.dto.*;
import com.habittracker.habit.exception.InvalidRequestException;
import com.habittracker.habit.exception.ResourceNotFoundException;
import com.habittracker.habit.mapper.HabitMapper;
import com.habittracker.habit.model.Category;
import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitStreak;
import com.habittracker.habit.repository.CategoryRepository;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitStreakRepository;
import com.habittracker.habit.service.ScheduleValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitStreakRepository habitStreakRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ScheduleValidationService scheduleValidationService;

    @Mock
    private HabitMapper habitMapper;

    @InjectMocks
    private HabitServiceImpl habitService;

    @Captor
    private ArgumentCaptor<Habit> habitCaptor;

    @Captor
    private ArgumentCaptor<HabitStreak> habitStreakCaptor;

    // Test data
    private UUID userId;
    private UUID habitId;
    private UUID categoryId;
    private Habit testHabit;
    private Category testCategory;
    private HabitStreak testStreak;
    private HabitResponseDTO testHabitResponseDTO;
    private HabitRequestDTO testHabitRequestDTO;
    private HabitUpdateDTO testHabitUpdateDTO;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        
        // Setup test category
        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setUserId(userId);
        testCategory.setName("Test Category");
        testCategory.setColor("#FF5733");
        testCategory.setIcon("test-icon");
        
        // Setup test habit
        testHabit = new Habit();
        testHabit.setId(habitId);
        testHabit.setUserId(userId);
        testHabit.setName("Test Habit");
        testHabit.setFrequency("DAILY");
        testHabit.setDescription("Test Description");
        testHabit.setPriority(3);
        testHabit.setSchedule("WEEKDAYS");
        testHabit.setColor("#3498DB");
        testHabit.setIcon("test-habit-icon");
        testHabit.setCreatedAt(LocalDateTime.now().minusDays(5));
        testHabit.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testHabit.setCategory(testCategory);
        
        // Setup test streak
        testStreak = new HabitStreak();
        testStreak.setHabitId(habitId);
        testStreak.setCurrentStreak(5);
        testStreak.setBestStreak(10);
        testStreak.setCompletionRate(0.75);
        testStreak.setNextDueAt(LocalDateTime.now().plusDays(1));
        testStreak.setHabit(testHabit);
        
        testHabit.setStreak(testStreak);
        
        // Setup request DTOs
        testHabitRequestDTO = new HabitRequestDTO();
        testHabitRequestDTO.setUserId(userId);
        testHabitRequestDTO.setName("Test Habit");
        testHabitRequestDTO.setFrequency("DAILY");
        testHabitRequestDTO.setDescription("Test Description");
        testHabitRequestDTO.setPriority(3);
        testHabitRequestDTO.setSchedule("WEEKDAYS");
        testHabitRequestDTO.setCategoryId(categoryId);
        
        testHabitUpdateDTO = new HabitUpdateDTO();
        testHabitUpdateDTO.setName("Updated Habit");
        testHabitUpdateDTO.setFrequency("WEEKLY");
        testHabitUpdateDTO.setDescription("Updated Description");
        testHabitUpdateDTO.setPriority(2);
        
        // Setup response DTO
        testHabitResponseDTO = new HabitResponseDTO();
        testHabitResponseDTO.setId(habitId);
        testHabitResponseDTO.setUserId(userId);
        testHabitResponseDTO.setName("Test Habit");
        testHabitResponseDTO.setFrequency("DAILY");
        testHabitResponseDTO.setDescription("Test Description");
        testHabitResponseDTO.setPriority(3);
        testHabitResponseDTO.setCategoryId(categoryId);
        testHabitResponseDTO.setCategoryName("Test Category");
        testHabitResponseDTO.setCurrentStreak(5);
        testHabitResponseDTO.setBestStreak(10);
        testHabitResponseDTO.setCompletionRate(0.75);
        testHabitResponseDTO.setNextDueAt(testStreak.getNextDueAt());
    }

    @Nested
    @DisplayName("Create Habit Tests")
    class CreateHabitTests {

        @Test
        @DisplayName("Should create habit successfully")
        void shouldCreateHabitSuccessfully() {
            // Arrange
            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(testCategory));
            
            given(habitRepository.save(any(Habit.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            
            given(scheduleValidationService.calculateNextDueDate(any(Habit.class), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusDays(1));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

            // Act
            HabitResponseDTO result = habitService.createHabit(testHabitRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testHabitResponseDTO.getId());
            assertThat(result.getName()).isEqualTo(testHabitRequestDTO.getName());
            
            // Verify interactions
            verify(habitRepository).save(habitCaptor.capture());
            verify(habitStreakRepository).save(habitStreakCaptor.capture());
            
            // Verify captured values
            Habit capturedHabit = habitCaptor.getValue();
            assertThat(capturedHabit.getName()).isEqualTo(testHabitRequestDTO.getName());
            assertThat(capturedHabit.getFrequency()).isEqualTo(testHabitRequestDTO.getFrequency());
            
            HabitStreak capturedStreak = habitStreakCaptor.getValue();
            assertThat(capturedStreak.getCurrentStreak()).isEqualTo(0);
            assertThat(capturedStreak.getBestStreak()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Arrange
            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> habitService.createHabit(testHabitRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found with id");
        }
        
        @Test
        @DisplayName("Should create habit without category when categoryId not provided")
        void shouldCreateHabitWithoutCategoryWhenCategoryIdNotProvided() {
            // Arrange
            testHabitRequestDTO.setCategoryId(null);
            
            given(habitRepository.save(any(Habit.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            
            given(scheduleValidationService.calculateNextDueDate(any(Habit.class), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusDays(1));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

            // Act
            HabitResponseDTO result = habitService.createHabit(testHabitRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            
            // Verify repository interaction - never tried to find a category
            verify(categoryRepository, never()).findById(any());
            
            // Verify habit was created without category
            verify(habitRepository).save(habitCaptor.capture());
            assertThat(habitCaptor.getValue().getCategory()).isNull();
        }
        
        @Test
        @DisplayName("Should throw exception when invalid UUID provided")
        void shouldThrowExceptionWhenInvalidUuidProvided() {
            // Arrange
            // First make sure the category lookup succeeds
            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(testCategory));
                
            // Then make the save operation throw the exception we want to test
            given(habitRepository.save(any(Habit.class)))
                .willThrow(new IllegalArgumentException("Test invalid UUID exception"));

            // Act & Assert
            assertThatThrownBy(() -> habitService.createHabit(testHabitRequestDTO))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Invalid request data");
        }
    }

    @Nested
    @DisplayName("Get Habit Tests")
    class GetHabitTests {

        @Test
        @DisplayName("Should get habit by id successfully")
        void shouldGetHabitByIdSuccessfully() {
            // Arrange
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            given(habitMapper.toResponseDTO(testHabit))
                    .willReturn(testHabitResponseDTO);

            // Act
            HabitResponseDTO result = habitService.getHabitResponseById(habitId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(habitId);
            assertThat(result.getName()).isEqualTo(testHabit.getName());
        }
        
        @Test
        @DisplayName("Should throw exception when habit not found")
        void shouldThrowExceptionWhenHabitNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            given(habitRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> habitService.getHabitResponseById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Habit not found with id");
        }
        
        @Test
        @DisplayName("Should get habits by userId with pagination")
        void shouldGetHabitsByUserIdWithPagination() {
            // Arrange
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String sortDir = "asc";
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            
            List<Habit> habits = Arrays.asList(testHabit);
            Page<Habit> habitPage = new PageImpl<>(habits, pageable, habits.size());
            
            given(habitRepository.findAllByUserIdPaged(userId, pageable))
                    .willReturn(habitPage);
            
            given(habitMapper.toResponseDTO(testHabit))
                    .willReturn(testHabitResponseDTO);

            // Act
            PagedResponseDTO<HabitResponseDTO> result = 
                    habitService.getHabitsByUserIdWithPagination(userId, page, size, sortBy, sortDir);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPageNo()).isEqualTo(page);
            assertThat(result.getPageSize()).isEqualTo(size);
            assertThat(result.getTotalElements()).isEqualTo(habits.size());
        }
        
        @Test
        @DisplayName("Should get habits by userId and categoryId with pagination")
        void shouldGetHabitsByUserIdAndCategoryIdWithPagination() {
            // Arrange
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String sortDir = "asc";
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            
            List<Habit> habits = Arrays.asList(testHabit);
            Page<Habit> habitPage = new PageImpl<>(habits, pageable, habits.size());
            
            given(categoryRepository.existsById(categoryId))
                    .willReturn(true);
            
            given(habitRepository.findAllByUserIdAndCategoryIdPaged(userId, categoryId, pageable))
                    .willReturn(habitPage);
            
            given(habitMapper.toResponseDTO(testHabit))
                    .willReturn(testHabitResponseDTO);

            // Act
            PagedResponseDTO<HabitResponseDTO> result = 
                    habitService.getHabitsByUserIdAndCategoryWithPagination(
                            userId, categoryId, page, size, sortBy, sortDir);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPageNo()).isEqualTo(page);
            assertThat(result.getPageSize()).isEqualTo(size);
            assertThat(result.getTotalElements()).isEqualTo(habits.size());
        }
        
        @Test
        @DisplayName("Should throw exception when category not exists for pagination")
        void shouldThrowExceptionWhenCategoryNotExistsForPagination() {
            // Arrange
            UUID nonExistentCategoryId = UUID.randomUUID();
            
            given(categoryRepository.existsById(nonExistentCategoryId))
                    .willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> 
                habitService.getHabitsByUserIdAndCategoryWithPagination(
                        userId, nonExistentCategoryId, 0, 10, "name", "asc"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id");
        }

        @Test
        @DisplayName("Should validate pagination parameters")
        void shouldValidatePaginationParameters() {
            // Act & Assert - Invalid page
            assertThatThrownBy(() -> 
                habitService.getHabitsByUserIdWithPagination(userId, -1, 10, "name", "asc"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Page number cannot be less than zero");
                
            // Act & Assert - Invalid size
            assertThatThrownBy(() -> 
                habitService.getHabitsByUserIdWithPagination(userId, 0, 0, "name", "asc"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Page size must be greater than zero");
                
            // Act & Assert - Size too large
            assertThatThrownBy(() -> 
                habitService.getHabitsByUserIdWithPagination(userId, 0, 101, "name", "asc"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Page size must not be greater than 100");
                
            // Act & Assert - Invalid sort field
            assertThatThrownBy(() -> 
                habitService.getHabitsByUserIdWithPagination(userId, 0, 10, "invalidField", "asc"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Sort parameter must be one of:");
        }
    }

    @Nested
    @DisplayName("Update Habit Tests")
    class UpdateHabitTests {

        @Test
        @DisplayName("Should update habit successfully")
        void shouldUpdateHabitSuccessfully() {
            // Arrange
            Habit updatedHabit = new Habit();
            updatedHabit.setId(habitId);
            updatedHabit.setName("Updated Habit");
            updatedHabit.setFrequency("WEEKLY");
            updatedHabit.setStreak(testStreak);
            
            HabitResponseDTO updatedResponseDTO = new HabitResponseDTO();
            updatedResponseDTO.setId(habitId);
            updatedResponseDTO.setName("Updated Habit");
            updatedResponseDTO.setFrequency("WEEKLY");
            
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            given(habitRepository.save(any(Habit.class)))
                    .willReturn(updatedHabit);
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(updatedResponseDTO);

            // Act
            HabitResponseDTO result = habitService.updateHabit(habitId, testHabitUpdateDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Habit");
            assertThat(result.getFrequency()).isEqualTo("WEEKLY");
            
            // Verify interactions
            verify(habitMapper).updateHabitFromDTO(eq(testHabitUpdateDTO), any(Habit.class));
            verify(habitRepository).save(any(Habit.class));
        }
        
        @Test
        @DisplayName("Should throw exception when updating non-existent habit")
        void shouldThrowExceptionWhenUpdatingNonExistentHabit() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            
            given(habitRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> habitService.updateHabit(nonExistentId, testHabitUpdateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Habit not found with id");
        }
        
        @Test
        @DisplayName("Should update category when categoryId provided")
        void shouldUpdateCategoryWhenCategoryIdProvided() {
            // Arrange
            UUID newCategoryId = UUID.randomUUID();
            Category newCategory = new Category();
            newCategory.setId(newCategoryId);
            newCategory.setName("New Category");
            
            testHabitUpdateDTO.setCategoryId(newCategoryId);
            
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            given(categoryRepository.findById(newCategoryId))
                    .willReturn(Optional.of(newCategory));
            
            given(habitRepository.save(any(Habit.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

            // Act
            habitService.updateHabit(habitId, testHabitUpdateDTO);

            // Assert
            verify(habitRepository).save(habitCaptor.capture());
            Habit savedHabit = habitCaptor.getValue();
            assertThat(savedHabit.getCategory()).isEqualTo(newCategory);
        }
        
        @Test
        @DisplayName("Should remove category when empty categoryId provided")
        void shouldRemoveCategoryWhenEmptyCategoryIdProvided() {
            // Arrange
            testHabitUpdateDTO.setCategoryId(null);
            
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            given(habitRepository.save(any(Habit.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

                
            // Mock the mapper behavior to set category to null
            doAnswer(invocation -> {
                Habit habit = invocation.getArgument(1);
                habit.setCategory(null);  // Actually set the category to null
                return null;
            }).when(habitMapper).updateHabitFromDTO(eq(testHabitUpdateDTO), any(Habit.class));

            // Act
            habitService.updateHabit(habitId, testHabitUpdateDTO);

            // Assert
            verify(habitRepository).save(habitCaptor.capture());
            Habit savedHabit = habitCaptor.getValue();
            assertThat(savedHabit.getCategory()).isNull();
        }
        
        @Test
        @DisplayName("Should recalculate next due date when frequency changes")
        void shouldRecalculateNextDueDateWhenFrequencyChanges() {
            // Arrange
            LocalDateTime newDueDate = LocalDateTime.now().plusDays(7);
            
            testHabit.setFrequency("DAILY");
            testHabitUpdateDTO.setFrequency("WEEKLY");
            
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            given(scheduleValidationService.calculateNextDueDate(any(Habit.class), any(LocalDateTime.class)))
                    .willReturn(newDueDate);
            
            given(habitRepository.save(any(Habit.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);
                    
            // THIS IS THE MISSING PART - Mock the updateHabitFromDTO to actually update the habit
            doAnswer(invocation -> {
                HabitUpdateDTO dto = invocation.getArgument(0);
                Habit habit = invocation.getArgument(1);
                habit.setFrequency(dto.getFrequency());  // Set the frequency from the DTO to the habit
                return null;  // Method returns void
            }).when(habitMapper).updateHabitFromDTO(any(HabitUpdateDTO.class), any(Habit.class));

            // Act
            habitService.updateHabit(habitId, testHabitUpdateDTO);

            // Assert
            verify(scheduleValidationService).calculateNextDueDate(any(Habit.class), any(LocalDateTime.class));
            verify(habitStreakRepository).save(habitStreakCaptor.capture());
            
            HabitStreak savedStreak = habitStreakCaptor.getValue();
            assertThat(savedStreak.getNextDueAt()).isEqualTo(newDueDate);
        }
    }

    @Nested
    @DisplayName("Delete Habit Tests")
    class DeleteHabitTests {

        @Test
        @DisplayName("Should delete habit successfully")
        void shouldDeleteHabitSuccessfully() {
            // Arrange
            given(habitRepository.findById(habitId))
                    .willReturn(Optional.of(testHabit));
            
            // Act
            DeleteResponseDTO result = habitService.deleteHabit(habitId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(habitId);
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getMessage()).contains("successfully deleted");
            
            verify(habitRepository).delete(testHabit);
        }
        
        @Test
        @DisplayName("Should throw exception when deleting non-existent habit")
        void shouldThrowExceptionWhenDeletingNonExistentHabit() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            
            given(habitRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> habitService.deleteHabit(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Habit not found with id");
        }
    }

    @Nested
    @DisplayName("Specialized Habit Query Tests")
    class SpecializedHabitQueryTests {

        @Test
        @DisplayName("Should get due habits")
        void shouldGetDueHabits() {
            // Arrange
            given(habitRepository.findDueHabits(eq(userId), any(LocalDateTime.class)))
                    .willReturn(Collections.singletonList(testHabit));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

            // Act
            List<HabitResponseDTO> result = habitService.getDueHabitsResponse(userId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(habitId);
            
            verify(habitRepository).findDueHabits(eq(userId), any(LocalDateTime.class));
        }
        
        @Test
        @DisplayName("Should get high priority habits")
        void shouldGetHighPriorityHabits() {
            // Arrange
            Integer minPriority = 3;
            
            given(habitRepository.findByUserIdAndMinimumPriority(userId, minPriority))
                    .willReturn(Collections.singletonList(testHabit));
            
            given(habitMapper.toResponseDTO(any(Habit.class)))
                    .willReturn(testHabitResponseDTO);

            // Act
            List<HabitResponseDTO> result = habitService.getHighPriorityHabitsResponse(userId, minPriority);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPriority()).isGreaterThanOrEqualTo(minPriority);
            
            verify(habitRepository).findByUserIdAndMinimumPriority(userId, minPriority);
        }
        
        @Test
        @DisplayName("Should get habits by frequency")
        void shouldGetHabitsByFrequency() {
            // Arrange
            String frequency = "DAILY";
            
            given(habitRepository.findByUserIdAndFrequency(userId, frequency))
                    .willReturn(Collections.singletonList(testHabit));

            // Act
            List<Habit> result = habitService.getHabitsByFrequency(userId, frequency);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFrequency()).isEqualTo(frequency);
        }
        
        @Test
        @DisplayName("Should get recently created habits")
        void shouldGetRecentlyCreatedHabits() {
            // Arrange
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            
            given(habitRepository.findByUserIdAndCreatedAtBetween(
                    eq(userId), eq(since), any(LocalDateTime.class)))
                    .willReturn(Collections.singletonList(testHabit));

            // Act
            List<Habit> result = habitService.getRecentlyCreatedHabits(userId, since);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testHabit);
        }
        
        @Test
        @DisplayName("Should get unused habits")
        void shouldGetUnusedHabits() {
            // Arrange
            given(habitRepository.findUnusedHabitsByUserId(userId))
                    .willReturn(Collections.singletonList(testHabit));

            // Act
            List<Habit> result = habitService.getUnusedHabits(userId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testHabit);
        }
    }

    // Helper methods for creating test data
    private List<Habit> createTestHabits(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                Habit habit = new Habit();
                habit.setId(UUID.randomUUID());
                habit.setUserId(userId);
                habit.setName("Habit " + i);
                habit.setFrequency(i % 2 == 0 ? "DAILY" : "WEEKLY");
                habit.setPriority(i % 5 + 1);
                habit.setCreatedAt(LocalDateTime.now().minusDays(i));
                return habit;
            })
            .collect(Collectors.toList());
    }
}
