package com.habittracker.habit.service;

import com.habittracker.habit.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    List<Category> getAllCategoriesByUserId(String userId);
    
    Optional<Category> getCategoryById(String id);
    
    Category createCategory(Category category);
    
    Category updateCategory(String id, Category categoryDetails);
    
    void deleteCategory(String id);
    
    Optional<Category> getCategoryByUserIdAndName(String userId, String name);
    
    Long countHabitsInCategory(String categoryId);
}
