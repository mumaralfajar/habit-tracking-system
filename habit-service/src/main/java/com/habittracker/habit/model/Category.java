package com.habittracker.habit.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    private String color;
    
    private String icon;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Habit> habits = new HashSet<>();
}
