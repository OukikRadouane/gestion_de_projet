package com.gestionprojet.model;

import com.gestionprojet.model.enums.SprintStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String goal; // Objectif du sprint (Sprint Goal)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status = SprintStatus.PLANNED;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Constructeur pour faciliter la création
    public Sprint(String name, LocalDate startDate, LocalDate endDate, Project project) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.project = project;
        this.status = SprintStatus.PLANNED;
    }

    // Méthode utilitaire pour calculer la durée en jours
    public long getDurationInDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    // Méthode pour vérifier si le sprint est actif
    public boolean isActive() {
        if (startDate == null || endDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate) && status == SprintStatus.ACTIVE;
    }

    // Méthode pour vérifier si le sprint est terminé
    public boolean isCompleted() {
        return status == SprintStatus.COMPLETED || 
               (endDate != null && endDate.isBefore(LocalDate.now()) && status != SprintStatus.CANCELLED);
    }
}