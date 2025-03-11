package com.habittracker.habit.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "habit_streaks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitStreak {
    
    @Id
    private Long habitId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "habit_id")
    private Habit habit;
    
    @Column(name = "current_streak")
    private Integer currentStreak;
    
    @Column(name = "best_streak")
    private Integer bestStreak;
    
    @Column(name = "last_completed_at")
    private LocalDateTime lastCompletedAt;
    
    @Column(name = "next_due_at")
    private LocalDateTime nextDueAt;
    
    @Column(name = "completion_rate")
    private Double completionRate;
}
