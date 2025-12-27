package com.gestionprojet.model.Tasks;

import com.gestionprojet.model.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TO_DO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false)
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskLog> logs = new ArrayList<>();

    public Task() {
        this.status = TaskStatus.TO_DO;
        this.priority = Priority.MEDIUM;
    }

    public Task(String title, String description, TaskStatus status, Priority priority, LocalDate deadline,
            User assignee, Sprint sprint) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.assignee = assignee;
        this.sprint = sprint;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
        comment.setTask(this);
    }

    public void removeComment(Comment comment) {
        if (comments != null && comments.contains(comment)) {
            comments.remove(comment);
            comment.setTask(null);
        }
    }

    public void addSubtask(Subtask subtask) {
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }
        subtasks.add(subtask);
        subtask.setTask(this);
    }

    public void removeSubtask(Subtask subtask) {
        if (subtasks != null && subtasks.contains(subtask)) {
            subtasks.remove(subtask);
            subtask.setTask(null);
        }
    }

    public void addLog(String message, User user) {
        TaskLog log = new TaskLog();
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        log.setTask(this);
        log.setUser(user);
        logs.add(log);
    }

    public List<TaskLog> getLogs() {
        return logs;
    }

    public void setLogs(List<TaskLog> logs) {
        this.logs = logs;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

}
