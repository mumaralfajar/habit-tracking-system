package com.habittracker.habit.service.impl;

import com.habittracker.habit.exception.InvalidRequestException;
import com.habittracker.habit.exception.ResourceNotFoundException;
import com.habittracker.habit.model.Category;
import com.habittracker.habit.model.Habit;
import com.habittracker.habit.repository.CategoryRepository;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final HabitRepository habitRepository;
    
    @Override
    public List<Category> getAllCategoriesByUserId(String userId) {
        return categoryRepository.findAllByUserId(userId);
    }
    
    @Override
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }
    
    @Override
    public Category createCategory(Category category) {
        // Check if category with same name exists for this user
        if (categoryRepository.existsByUserIdAndName(
                category.getUserId().toString(), category.getName())) {
            throw new InvalidRequestException("Category with name '" + category.getName() + 
                    "' already exists for this user");
        }
        
        // Add default color if not provided
        if (category.getColor() == null || category.getColor().isEmpty()) {
            category.setColor("#3498db"); // Default blue color
        }
        
        return categoryRepository.save(category);
    }
    
    @Override
    public Category updateCategory(String id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryDetails.getName())) {
            if (categoryRepository.existsByUserIdAndName(
                    category.getUserId().toString(), categoryDetails.getName())) {
                throw new InvalidRequestException("Category with name '" + categoryDetails.getName() + 
                        "' already exists for this user");
            }
        }
        
        category.setName(categoryDetails.getName());
        category.setColor(categoryDetails.getColor());
        category.setIcon(categoryDetails.getIcon());
        
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Update habits to remove this category
        List<Habit> habitsWithCategory = habitRepository.findAllByUserIdAndCategoryId(
                category.getUserId().toString(), id);
        
        for (Habit habit : habitsWithCategory) {
            habit.setCategory(null);
            habitRepository.save(habit);
        }
        
        categoryRepository.delete(category);
    }
    
    @Override
    public Optional<Category> getCategoryByUserIdAndName(String userId, String name) {
        return categoryRepository.findByUserIdAndName(userId, name);
    }
    
    @Override
    public Long countHabitsInCategory(String categoryId) {
        // Check if category exists first
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
                
        return categoryRepository.countHabitsByCategoryId(categoryId);
    }
}
