package com.habittracker.habit.controller;

import com.habittracker.habit.dto.CategoryRequestDTO;
import com.habittracker.habit.dto.CategoryResponseDTO;
import com.habittracker.habit.dto.CategoryUpdateDTO;
import com.habittracker.habit.dto.DeleteResponseDTO;
import com.habittracker.habit.exception.ResourceNotFoundException;
import com.habittracker.habit.mapper.CategoryMapper;
import com.habittracker.habit.model.Category;
import com.habittracker.habit.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    /**
     * Create a new category
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        Category category = categoryMapper.toEntity(requestDTO);
        Category createdCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(categoryMapper.toResponseDTO(createdCategory), HttpStatus.CREATED);
    }

    /**
     * Get all categories for a user
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByUserId(@RequestParam String userId) {
        List<Category> categories = categoryService.getAllCategoriesByUserId(userId);
        List<CategoryResponseDTO> responseDTOs = categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Get a specific category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable String id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return ResponseEntity.ok(categoryMapper.toResponseDTO(category));
    }

    /**
     * Update a category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable String id, 
            @Valid @RequestBody CategoryUpdateDTO updateDTO) {
        Category updatedCategory = categoryService.updateCategory(id, convertForUpdate(id, updateDTO));
        return ResponseEntity.ok(categoryMapper.toResponseDTO(updatedCategory));
    }

    /**
     * Delete a category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponseDTO> deleteCategory(@PathVariable String id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        String categoryName = category.getName();
        categoryService.deleteCategory(id);
        
        DeleteResponseDTO response = DeleteResponseDTO.builder()
                .id(id)
                .message("Category '" + categoryName + "' successfully deleted")
                .timestamp(LocalDateTime.now())
                .successful(true)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Find category by name for a specific user
     */
    @GetMapping("/search")
    public ResponseEntity<CategoryResponseDTO> getCategoryByName(
            @RequestParam String userId,
            @RequestParam String name) {
        return categoryService.getCategoryByUserIdAndName(userId, name)
                .map(categoryMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Count habits in a category
     */
    @GetMapping("/{id}/habits/count")
    public ResponseEntity<Long> countHabitsInCategory(@PathVariable String id) {
        Long count = categoryService.countHabitsInCategory(id);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Helper method to convert update DTO to entity for the service layer
     */
    private Category convertForUpdate(String id, CategoryUpdateDTO updateDTO) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        Category updatedCategory = new Category();
        updatedCategory.setId(category.getId());
        updatedCategory.setUserId(category.getUserId());
        updatedCategory.setName(updateDTO.getName());
        updatedCategory.setColor(updateDTO.getColor());
        updatedCategory.setIcon(updateDTO.getIcon());
        
        return updatedCategory;
    }
}
