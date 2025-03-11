package com.habittracker.habit.repository;

import com.habittracker.habit.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    
    List<Habit> findAllByUserId(String userId);
    
    List<Habit> findAllByUserIdAndCategoryId(String userId, Long categoryId);
    
    @Query("SELECT h FROM Habit h JOIN h.streak s WHERE h.userId = :userId AND s.nextDueAt <= :currentTime")
    List<Habit> findDueHabits(String userId, LocalDateTime currentTime);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.frequency = :frequency")
    List<Habit> findByUserIdAndFrequency(String userId, String frequency);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.createdAt BETWEEN :startDate AND :endDate")
    List<Habit> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT h FROM Habit h WHERE h.userId = :userId AND h.priority >= :minPriority")
    List<Habit> findByUserIdAndMinimumPriority(String userId, Integer minPriority);
    
    @Query("SELECT h FROM Habit h LEFT JOIN h.trackingRecords r WHERE h.userId = :userId " +
           "GROUP BY h HAVING COUNT(r) = 0")
    List<Habit> findUnusedHabitsByUserId(String userId);
}
