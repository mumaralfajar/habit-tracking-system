package com.habittracker.habit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateDTO {
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String color;
    private String icon;
}
