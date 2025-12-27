package com.gestionprojet.model.Tasks;

public enum TaskStatus {
    BACKLOG("Backlog"),
    TO_DO("To Do"),
    DOING("DOING"),
    DONE("Done");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
