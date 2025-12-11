package com.gestionprojet.model.Tasks;

public enum TaskStatus {
    TO_DO("To Do"),
    DOING("Doing"),
    DONE("Done");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
