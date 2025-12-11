package com.gestionprojet.model.Tasks;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String message;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public TaskLog() { }

    public TaskLog(String message, Task task) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.task = task;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}
