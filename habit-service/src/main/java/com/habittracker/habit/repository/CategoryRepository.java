package com.habittracker.habit.repository;

import com.habittracker.habit.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    List<Category> findAllByUserId(String userId);
    
    Optional<Category> findByUserIdAndName(String userId, String name);
    
    @Query("SELECT c FROM Category c WHERE c.userId = :userId ORDER BY c.name ASC")
    List<Category> findAllByUserIdOrderByNameAsc(String userId);
    
    @Query("SELECT COUNT(h) FROM Habit h WHERE h.category.id = :categoryId")
    Long countHabitsByCategoryId(String categoryId);
    
    @Query("SELECT c FROM Category c WHERE c.userId = :userId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.color) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> searchCategoriesByUserIdAndTerm(String userId, String searchTerm);
    
    boolean existsByUserIdAndName(String userId, String name);
}
