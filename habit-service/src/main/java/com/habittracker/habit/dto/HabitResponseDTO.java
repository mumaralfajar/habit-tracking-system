package com.habittracker.habit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitResponseDTO {
    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private String frequency;
    private String schedule;
    private Integer priority;
    private String color;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID categoryId;
    private String categoryName;

    private Integer currentStreak;
    private Integer bestStreak;
    private Double completionRate;
    private LocalDateTime nextDueAt;
}
