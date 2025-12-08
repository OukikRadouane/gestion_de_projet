package com.gestionprojet.model.enums;

public enum SprintStatus {
    PLANNED("Planifié"),
    ACTIVE("Actif"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé");

    private final String label;

    SprintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

