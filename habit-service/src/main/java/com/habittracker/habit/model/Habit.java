package com.habittracker.habit.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String frequency;  // DAILY, WEEKLY, MONTHLY, X_TIMES_PER_WEEK, etc.
    
    private String schedule;   // Serialized schedule data (JSON)
    
    @Column(name = "time_of_day")
    private String timeOfDay;  // MORNING, AFTERNOON, EVENING, ANY, or specific time
    
    private Integer priority;  // 1-5 scale
    
    private String color;
    
    private String icon;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HabitTrackingRecord> trackingRecords = new HashSet<>();
    
    @OneToOne(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private HabitStreak streak;
    
    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HabitReminder> reminders = new HashSet<>();
}
