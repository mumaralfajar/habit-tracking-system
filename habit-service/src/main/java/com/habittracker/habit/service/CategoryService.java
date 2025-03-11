package com.habittracker.habit.service;

import com.habittracker.habit.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    List<Category> getAllCategoriesByUserId(String userId);
    
    Optional<Category> getCategoryById(Long id);
    
    Category createCategory(Category category);
    
    Category updateCategory(Long id, Category categoryDetails);
    
    void deleteCategory(Long id);
    
    Optional<Category> getCategoryByUserIdAndName(String userId, String name);
    
    Long countHabitsInCategory(Long categoryId);
}
