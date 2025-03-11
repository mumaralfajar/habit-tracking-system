package com.habittracker.habit.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "habit_tracking_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitTrackingRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    private String notes;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Min(1)
    @Max(5)
    @Column(name = "mood_rating")
    private Integer moodRating;
    
    @Min(1)
    @Max(5)
    @Column(name = "difficulty_rating")
    private Integer difficultyRating;
}
