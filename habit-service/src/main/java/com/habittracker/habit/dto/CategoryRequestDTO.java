package com.habittracker.habit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String color;
    private String icon;
}
