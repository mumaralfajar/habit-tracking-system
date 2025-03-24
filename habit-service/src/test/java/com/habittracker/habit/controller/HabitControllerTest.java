package com.habittracker.habit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.habit.dto.*;
import com.habittracker.habit.exception.GlobalExceptionHandler;
import com.habittracker.habit.exception.ResourceNotFoundException;
import com.habittracker.habit.service.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class HabitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HabitService habitService;

    @InjectMocks
    private HabitController habitController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UUID habitId;
    private UUID categoryId;

    @BeforeEach
    void setup() {
        // Initialize object mapper
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For handling Java 8 date/time types

        // Set up MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(habitController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        // Initialize test data
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    // Test fixtures
    private HabitRequestDTO createValidHabitRequest() {
        return HabitRequestDTO.builder()
                .userId(userId)
                .name("Morning Meditation")
                .frequency("DAILY")
                .description("15 minutes of mindfulness meditation")
                .priority(3)
                .categoryId(categoryId)
                .schedule("MORNING")
                .startDate(LocalDateTime.now())
                .color("#4CAF50")
                .targetCompletions(30)
                .icon("meditation")
                .build();
    }

    private HabitResponseDTO createHabitResponse() {
        return HabitResponseDTO.builder()
                .id(habitId)
                .userId(userId)
                .name("Morning Meditation")
                .description("15 minutes of mindfulness meditation")
                .frequency("DAILY")
                .schedule("MORNING")
                .priority(3)
                .color("#4CAF50")
                .icon("meditation")
                .categoryId(categoryId)
                .categoryName("Wellness")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .currentStreak(5)
                .bestStreak(10)
                .completionRate(0.8)
                .nextDueAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    // Create Habit Tests
    @Test
    @DisplayName("Given valid request when creating habit then return created habit")
    void given_validHabitRequest_when_createHabit_then_returnCreatedHabit() throws Exception {
        // Given
        HabitRequestDTO requestDTO = createValidHabitRequest();
        HabitResponseDTO responseDTO = createHabitResponse();
        
        when(habitService.createHabit(any(HabitRequestDTO.class))).thenReturn(responseDTO);

        // When / Then
        mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(habitId.toString())))
                .andExpect(jsonPath("$.name", is("Morning Meditation")))
                .andExpect(jsonPath("$.userId", is(userId.toString())));

        verify(habitService).createHabit(any(HabitRequestDTO.class));
    }

    @Test
    @DisplayName("Given invalid request when creating habit then return validation errors")
    void given_invalidHabitRequest_when_createHabit_then_returnValidationErrors() throws Exception {
        // Given
        HabitRequestDTO requestDTO = new HabitRequestDTO(); // Empty request will trigger validation errors
        
        // When / Then
        mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(habitService, never()).createHabit(any(HabitRequestDTO.class));
    }

    // Get Habit By ID Tests
    @Test
    @DisplayName("Given existing habit ID when getting habit then return habit details")
    void given_existingHabitId_when_getHabitById_then_returnHabitDetails() throws Exception {
        // Given
        HabitResponseDTO responseDTO = createHabitResponse();
        when(habitService.getHabitResponseById(eq(habitId))).thenReturn(responseDTO);

        // When / Then
        mockMvc.perform(get("/api/habits/{id}", habitId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(habitId.toString())))
                .andExpect(jsonPath("$.name", is("Morning Meditation")))
                .andExpect(jsonPath("$.frequency", is("DAILY")));

        verify(habitService).getHabitResponseById(eq(habitId));
    }

    @Test
    @DisplayName("Given non-existing habit ID when getting habit then throw ResourceNotFoundException")
    void given_nonExistingHabitId_when_getHabitById_then_throwResourceNotFoundException() throws Exception {
        // Given
        when(habitService.getHabitResponseById(eq(habitId)))
                .thenThrow(new ResourceNotFoundException("Habit not found with id: " + habitId));

        // When / Then
        mockMvc.perform(get("/api/habits/{id}", habitId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(habitService).getHabitResponseById(eq(habitId));
    }

    // Get Habits By User ID Tests
    @Test
    @DisplayName("Given user ID when getting user's habits then return paginated habits")
    void given_userId_when_getHabitsByUserId_then_returnPaginatedHabits() throws Exception {
        // Given
        List<HabitResponseDTO> habits = Arrays.asList(createHabitResponse(), createHabitResponse());
        PagedResponseDTO<HabitResponseDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(habits);
        pagedResponse.setPageNo(0);
        pagedResponse.setPageSize(10);
        pagedResponse.setTotalElements(2);
        pagedResponse.setTotalPages(1);
        pagedResponse.setLast(true);
        
        when(habitService.getHabitsByUserIdWithPagination(eq(userId), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(pagedResponse);

        // When / Then
        mockMvc.perform(get("/api/habits/user/{userId}", userId)
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.pageSize", is(10)));

        verify(habitService).getHabitsByUserIdWithPagination(eq(userId), eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("Given user ID and category ID when getting habits then return filtered paginated habits")
    void given_userIdAndCategoryId_when_getHabitsByUserIdAndCategory_then_returnFilteredPaginatedHabits() throws Exception {
        // Given
        List<HabitResponseDTO> habits = Collections.singletonList(createHabitResponse());
        PagedResponseDTO<HabitResponseDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(habits);
        pagedResponse.setPageNo(0);
        pagedResponse.setPageSize(10);
        pagedResponse.setTotalElements(1);
        pagedResponse.setTotalPages(1);
        pagedResponse.setLast(true);
        
        when(habitService.getHabitsByUserIdAndCategoryWithPagination(
                eq(userId), eq(categoryId), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(pagedResponse);

        // When / Then
        mockMvc.perform(get("/api/habits/user/{userId}", userId)
                .param("page", "0")
                .param("size", "10")
                .param("categoryId", categoryId.toString())
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryId", is(categoryId.toString())));

        verify(habitService).getHabitsByUserIdAndCategoryWithPagination(
                eq(userId), eq(categoryId), eq(0), eq(10), eq("name"), eq("asc"));
    }

    // Get High Priority Habits Tests
    @Test
    @DisplayName("Given user ID when getting high priority habits then return habits list")
    void given_userId_when_getHighPriorityHabits_then_returnHabitsList() throws Exception {
        // Given
        List<HabitResponseDTO> habits = Arrays.asList(createHabitResponse(), createHabitResponse());
        
        when(habitService.getHighPriorityHabitsResponse(eq(userId), eq(3)))
                .thenReturn(habits);

        // When / Then
        mockMvc.perform(get("/api/habits/user/{userId}/priority", userId)
                .param("minPriority", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].priority", is(3)));

        verify(habitService).getHighPriorityHabitsResponse(eq(userId), eq(3));
    }

    // Get Due Habits Tests
    @Test
    @DisplayName("Given user ID when getting due habits then return habits list")
    void given_userId_when_getDueHabits_then_returnHabitsList() throws Exception {
        // Given
        List<HabitResponseDTO> habits = Arrays.asList(createHabitResponse(), createHabitResponse());
        
        when(habitService.getDueHabitsResponse(eq(userId)))
                .thenReturn(habits);

        // When / Then
        mockMvc.perform(get("/api/habits/user/{userId}/due", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(habitService).getDueHabitsResponse(eq(userId));
    }

    // Update Habit Tests
    @Test
    @DisplayName("Given valid update request when updating habit then return updated habit")
    void given_validUpdateRequest_when_updateHabit_then_returnUpdatedHabit() throws Exception {
        // Given
        HabitUpdateDTO updateDTO = HabitUpdateDTO.builder()
                .name("Updated Meditation")
                .frequency("DAILY")
                .description("20 minutes of mindfulness meditation")
                .priority(4)
                .build();
        
        HabitResponseDTO responseDTO = HabitResponseDTO.builder()
                .id(habitId)
                .userId(userId)
                .name("Updated Meditation")
                .description("20 minutes of mindfulness meditation")
                .frequency("DAILY")
                .priority(4)
                .categoryId(categoryId)
                .categoryName("Wellness")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(habitService.updateHabit(eq(habitId), any(HabitUpdateDTO.class)))
                .thenReturn(responseDTO);

        // When / Then
        mockMvc.perform(put("/api/habits/{id}", habitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(habitId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Meditation")))
                .andExpect(jsonPath("$.priority", is(4)));

        verify(habitService).updateHabit(eq(habitId), any(HabitUpdateDTO.class));
    }

    @Test
    @DisplayName("Given invalid update request when updating habit then return validation errors")
    void given_invalidUpdateRequest_when_updateHabit_then_returnValidationErrors() throws Exception {
        // Given
        HabitUpdateDTO updateDTO = new HabitUpdateDTO(); // Missing required fields
        
        // When / Then
        mockMvc.perform(put("/api/habits/{id}", habitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(habitService, never()).updateHabit(any(), any());
    }

    @Test
    @DisplayName("Given non-existing habit when updating habit then return not found")
    void given_nonExistingHabit_when_updateHabit_then_returnNotFound() throws Exception {
        // Given
        HabitUpdateDTO updateDTO = HabitUpdateDTO.builder()
                .name("Updated Meditation")
                .frequency("DAILY")
                .build();
        
        when(habitService.updateHabit(eq(habitId), any(HabitUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Habit not found with id: " + habitId));

        // When / Then
        mockMvc.perform(put("/api/habits/{id}", habitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(habitService).updateHabit(eq(habitId), any(HabitUpdateDTO.class));
    }

    // Delete Habit Tests
    @Test
    @DisplayName("Given existing habit ID when deleting habit then return success message")
    void given_existingHabitId_when_deleteHabit_then_returnSuccessMessage() throws Exception {
        // Given
        DeleteResponseDTO responseDTO = DeleteResponseDTO.builder()
                .id(habitId)
                .message("Habit deleted successfully")
                .timestamp(LocalDateTime.now())
                .successful(true)
                .build();
                
        when(habitService.deleteHabit(eq(habitId))).thenReturn(responseDTO);

        // When / Then
        mockMvc.perform(delete("/api/habits/{id}", habitId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful", is(true)))
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));

        verify(habitService).deleteHabit(eq(habitId));
    }

    @Test
    @DisplayName("Given non-existing habit ID when deleting habit then return not found")
    void given_nonExistingHabitId_when_deleteHabit_then_returnNotFound() throws Exception {
        // Given
        when(habitService.deleteHabit(eq(habitId)))
                .thenThrow(new ResourceNotFoundException("Habit not found with id: " + habitId));

        // When / Then
        mockMvc.perform(delete("/api/habits/{id}", habitId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(habitService).deleteHabit(eq(habitId));
    }
}
