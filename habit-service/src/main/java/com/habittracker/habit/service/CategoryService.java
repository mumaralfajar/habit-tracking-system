package com.habittracker.habit.service;

import com.habittracker.habit.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {
    
    List<Category> getAllCategoriesByUserId(UUID userId);
    
    Optional<Category> getCategoryById(UUID id);
    
    Category createCategory(Category category);
    
    Category updateCategory(UUID id, Category categoryDetails);
    
    void deleteCategory(UUID id);
    
    Optional<Category> getCategoryByUserIdAndName(UUID userId, String name);
    
    Long countHabitsInCategory(UUID categoryId);
}
