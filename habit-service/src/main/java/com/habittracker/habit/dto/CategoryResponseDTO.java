package com.habittracker.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String color;
    private String icon;
    private Long habitCount; // Optional count of habits in this category
}
