package com.gestionprojet.view;

/**
 * Simple Task class for UI purposes, independent from the domain model.
 * Used for Kanban board visualization until proper integration is implemented.
 */
public class KanbanTask {
    private String title;
    private String description;
    private TaskStatus status;

    public enum TaskStatus {
        TODO, DOING, DONE
    }

    public KanbanTask(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
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
}
