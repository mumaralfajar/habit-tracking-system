package com.habittracker.habit.repository;

import com.habittracker.habit.model.HabitReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HabitReminderRepository extends JpaRepository<HabitReminder, UUID> {
    
    List<HabitReminder> findByHabitId(UUID habitId);
    
    @Query("SELECT r FROM HabitReminder r JOIN r.habit h WHERE h.userId = :userId AND r.enabled = true")
    List<HabitReminder> findActiveRemindersByUserId(UUID userId);
    
    @Query("SELECT r FROM HabitReminder r WHERE r.enabled = true AND r.remindAt BETWEEN :startTime AND :endTime")
    List<HabitReminder> findActiveRemindersDueWithinTimeRange(LocalTime startTime, LocalTime endTime);
    
    @Query("SELECT r FROM HabitReminder r JOIN r.habit h WHERE h.userId = :userId AND r.daysOfWeek LIKE %:dayOfWeek% AND r.enabled = true")
    List<HabitReminder> findActiveRemindersByUserIdAndDayOfWeek(UUID userId, String dayOfWeek);
    
    @Query("SELECT r FROM HabitReminder r JOIN r.habit h WHERE h.userId = :userId AND r.notificationType = :notificationType AND r.enabled = true")
    List<HabitReminder> findActiveRemindersByUserIdAndNotificationType(UUID userId, String notificationType);
}
