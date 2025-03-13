package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Category;
import com.habittracker.habit.model.Habit;
import com.habittracker.habit.repository.CategoryRepository;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
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
        Optional<Category> existing = categoryRepository.findByUserIdAndName(
                category.getUserId().toString(), category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Category with name " + category.getName() + 
                    " already exists for this user");
        }
        return categoryRepository.save(category);
    }
    
    @Override
    public Category updateCategory(String id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryDetails.getName())) {
            Optional<Category> existingWithName = categoryRepository.findByUserIdAndName(
                    category.getUserId().toString(), categoryDetails.getName());
            if (existingWithName.isPresent()) {
                throw new IllegalArgumentException("Category with name " + categoryDetails.getName() + 
                        " already exists for this user");
            }
        }
        
        category.setName(categoryDetails.getName());
        category.setColor(categoryDetails.getColor());
        category.setIcon(categoryDetails.getIcon());
        
        return categoryRepository.save(category);
    }
    
    @Override
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Update habits to remove this category
        for (Habit habit : category.getHabits()) {
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
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return (long) category.getHabits().size();
    }
}
