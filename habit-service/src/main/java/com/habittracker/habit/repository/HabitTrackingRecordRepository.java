package com.habittracker.habit.repository;

import com.habittracker.habit.model.HabitTrackingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitTrackingRecordRepository extends JpaRepository<HabitTrackingRecord, Long> {
    
    List<HabitTrackingRecord> findByHabitId(Long habitId);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.id = :habitId AND r.completedAt BETWEEN :start AND :end ORDER BY r.completedAt DESC")
    List<HabitTrackingRecord> findByHabitIdAndCompletedAtBetween(Long habitId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.userId = :userId AND r.completedAt BETWEEN :start AND :end ORDER BY r.completedAt DESC")
    List<HabitTrackingRecord> findByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM HabitTrackingRecord r WHERE r.habit.id = :habitId AND r.completedAt BETWEEN :start AND :end")
    Long countCompletionsInRange(Long habitId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM HabitTrackingRecord r WHERE r.habit.id = :habitId ORDER BY r.completedAt DESC LIMIT 1")
    Optional<HabitTrackingRecord> findMostRecentByHabitId(Long habitId);
    
    @Query("SELECT r.habit.id, COUNT(r) as count, AVG(r.difficultyRating) as avgDifficulty, AVG(r.moodRating) as avgMood " +
           "FROM HabitTrackingRecord r " +
           "WHERE r.habit.userId = :userId AND r.completedAt BETWEEN :start AND :end " +
           "GROUP BY r.habit.id")
    List<Object[]> getCompletionStatsByUserIdBetweenDates(String userId, LocalDateTime start, LocalDateTime end);
}
