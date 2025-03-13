package com.habittracker.habit.repository;

import com.habittracker.habit.model.HabitStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitStreakRepository extends JpaRepository<HabitStreak, String> {
    
    Optional<HabitStreak> findByHabitId(String habitId);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId")
    List<HabitStreak> findByUserId(String userId);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId ORDER BY s.currentStreak DESC")
    List<HabitStreak> findByUserIdOrderByCurrentStreakDesc(String userId);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId ORDER BY s.bestStreak DESC")
    List<HabitStreak> findByUserIdOrderByBestStreakDesc(String userId);
    
    @Query("SELECT s FROM HabitStreak s WHERE s.nextDueAt < :currentTime AND s.currentStreak > 0")
    List<HabitStreak> findStreaksToReset(LocalDateTime currentTime);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId AND s.completionRate BETWEEN :minRate AND :maxRate")
    List<HabitStreak> findByCompletionRateRange(String userId, Double minRate, Double maxRate);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId AND s.currentStreak = 0 " +
           "AND s.lastCompletedAt > :since")
    List<HabitStreak> findRecentlyBrokenStreaks(String userId, LocalDateTime since);
    
    @Query("SELECT s FROM HabitStreak s JOIN s.habit h WHERE h.userId = :userId " +
           "ORDER BY (s.currentStreak * 1.0 / h.priority) DESC")
    List<HabitStreak> findStreaksByEfficiency(String userId);
}
