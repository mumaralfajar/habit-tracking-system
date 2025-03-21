package com.habittracker.habit.repository;

import com.habittracker.habit.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    List<Category> findAllByUserId(UUID userId);
    
    Optional<Category> findByUserIdAndName(UUID userId, String name);
    
    @Query("SELECT c FROM Category c WHERE c.userId = :userId ORDER BY c.name ASC")
    List<Category> findAllByUserIdOrderByNameAsc(UUID userId);
    
    @Query("SELECT COUNT(h) FROM Habit h WHERE h.category.id = :categoryId")
    Long countHabitsByCategoryId(UUID categoryId);
    
    @Query("SELECT c FROM Category c WHERE c.userId = :userId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.color) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> searchCategoriesByUserIdAndTerm(UUID userId, String searchTerm);
    
    boolean existsByUserIdAndName(UUID userId, String name);
}
