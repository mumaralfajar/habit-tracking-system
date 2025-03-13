package com.habittracker.habit.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "habit_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitReminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;
    
    @Column(name = "remind_at", nullable = false)
    private LocalTime remindAt;
    
    @Column(name = "days_of_week")
    private String daysOfWeek;  // Stored as comma-separated values: "MON,WED,FRI"
    
    @Column(name = "notification_type")
    private String notificationType;  // PUSH, EMAIL, SMS, etc.
    
    private String message;
    
    private Boolean enabled = true;
}
