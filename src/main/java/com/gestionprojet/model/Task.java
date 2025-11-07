package com.gestionprojet.model;


import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TO_DO;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    }

enum TaskStatus { TO_DO, DOING, DONE }
enum Priority { LOW, MEDIUM, HIGH, CRITICAL }