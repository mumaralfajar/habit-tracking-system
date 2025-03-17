package com.habittracker.habit.mapper;

import com.habittracker.habit.dto.CategoryRequestDTO;
import com.habittracker.habit.dto.CategoryResponseDTO;
import com.habittracker.habit.dto.CategoryUpdateDTO;
import com.habittracker.habit.model.Category;
import com.habittracker.habit.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryMapper {
    
    private final CategoryRepository categoryRepository;

    /**
     * Convert Category entity to CategoryResponseDTO
     */
    public CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) {
            return null;
        }

        Long habitCount = categoryRepository.countHabitsByCategoryId(category.getId().toString());
        
        return CategoryResponseDTO.builder()
                .id(category.getId().toString())
                .userId(category.getUserId().toString())
                .name(category.getName())
                .color(category.getColor())
                .icon(category.getIcon())
                .habitCount(habitCount)
                .build();
    }

    /**
     * Convert CategoryRequestDTO to Category entity
     */
    public Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(dto.getUserId()))
                .name(dto.getName())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .build();
    }

    /**
     * Update Category entity from CategoryUpdateDTO
     */
    public void updateEntityFromDTO(CategoryUpdateDTO dto, Category category) {
        if (dto == null || category == null) {
            return;
        }

        category.setName(dto.getName());
        
        if (dto.getColor() != null) {
            category.setColor(dto.getColor());
        }
        
        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
    }
}
