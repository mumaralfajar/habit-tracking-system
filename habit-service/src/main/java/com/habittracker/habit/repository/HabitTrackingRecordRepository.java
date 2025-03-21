package com.habittracker.habit.repository;

import com.habittracker.habit.model.HabitTrackingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitTrackingRecordRepository extends JpaRepository<HabitTrackingRecord, String> {
    
    List<HabitTrackingRecord> findByHabitId(UUID habitId);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.id = :habitId AND r.completedAt BETWEEN :start AND :end ORDER BY r.completedAt DESC")
    List<HabitTrackingRecord> findByHabitIdAndCompletedAtBetween(UUID habitId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.userId = :userId AND r.completedAt BETWEEN :start AND :end ORDER BY r.completedAt DESC")
    List<HabitTrackingRecord> findByUserIdAndCompletedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM HabitTrackingRecord r WHERE r.habit.id = :habitId AND r.completedAt BETWEEN :start AND :end")
    Long countCompletionsInRange(UUID habitId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.id = :habitId ORDER BY r.completedAt DESC LIMIT 1")
    Optional<HabitTrackingRecord> findMostRecentByHabitId(UUID habitId);
    
    @Query("SELECT r.habit.id, COUNT(r) as count, AVG(r.difficultyRating) as avgDifficulty, AVG(r.moodRating) as avgMood " +
           "FROM HabitTrackingRecord r " +
           "WHERE r.habit.userId = :userId AND r.completedAt BETWEEN :start AND :end " +
           "GROUP BY r.habit.id")
    List<Object[]> getCompletionStatsByUserIdBetweenDates(UUID userId, LocalDateTime start, LocalDateTime end);
}
