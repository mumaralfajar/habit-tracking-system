package com.habittracker.habit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String frequency;
    private String schedule;
    private Integer priority;
    private String color;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryId;
    private String categoryName;

    private Integer currentStreak;
    private Integer bestStreak;
    private Double completionRate;
    private LocalDateTime nextDueAt;
}
