package com.habittracker.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitUpdateDTO {
    // Required fields
    @NotBlank(message = "Habit name is required")
    private String name;
    
    @NotBlank(message = "Frequency is required")
    private String frequency;
    
    // Optional fields
    private String categoryId;
    private String description;
    private Integer priority;
    private String schedule;
    private String color;
    private String icon;
    private Boolean active;
    private Integer targetCompletions;
    private String timeOfDay;
}
