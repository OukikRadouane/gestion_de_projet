package com.gestionprojet.model.Tasks;

import jakarta.persistence.*;

@Entity
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task; // La tâche principale

    @Column(nullable = false)
    private String title; // Le titre de la sous-tâche

    @Column(nullable = false)
    private boolean done = false; // Statut fait ou non

    public Subtask() {
    }

    public Subtask(Task task, String title) {
        this.task = task;
        this.title = title;
        this.done = false;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Subtask subtask = (Subtask) o;
        return id != null && id.equals(subtask.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
