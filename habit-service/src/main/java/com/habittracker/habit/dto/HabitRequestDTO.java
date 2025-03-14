package com.habittracker.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitRequestDTO {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Habit name is required")
    private String name;
    
    @NotBlank(message = "Frequency is required")
    private String frequency;
    
    private String categoryId;
    private String description;
    private Integer priority; 
    private String schedule;
    private LocalDateTime startDate;
    private String color;
    private Integer targetCompletions;
    private String icon;
}
