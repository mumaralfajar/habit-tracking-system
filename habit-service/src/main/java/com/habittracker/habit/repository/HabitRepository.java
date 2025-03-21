package com.habittracker.habit.repository;

import com.habittracker.habit.model.Habit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {
    
    List<Habit> findAllByUserId(UUID userId);
    
    List<Habit> findAllByUserIdAndCategoryId(UUID userId, UUID categoryId);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId")
    Page<Habit> findAllByUserIdPaged(UUID userId, Pageable pageable);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.category.id = :categoryId")
    Page<Habit> findAllByUserIdAndCategoryIdPaged(UUID userId, UUID categoryId, Pageable pageable);
    
    @Query("SELECT h FROM Habit h JOIN h.streak s WHERE h.userId = :userId AND s.nextDueAt <= :currentTime")
    List<Habit> findDueHabits(UUID userId, LocalDateTime currentTime);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.frequency = :frequency")
    List<Habit> findByUserIdAndFrequency(UUID userId, String frequency);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.createdAt BETWEEN :startDate AND :endDate")
    List<Habit> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.priority >= :minPriority ORDER BY h.priority DESC")
    List<Habit> findByUserIdAndMinimumPriority(UUID userId, Integer minPriority);
    
    @Query("SELECT h FROM Habit h LEFT JOIN h.trackingRecords r WHERE h.userId = :userId " +
           "GROUP BY h HAVING COUNT(r) = 0")
    List<Habit> findUnusedHabitsByUserId(UUID userId);
}
