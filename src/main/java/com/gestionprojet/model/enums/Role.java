package com.gestionprojet.model.enums;

public enum Role {
    ADMIN("Administrateur"),
    USER("Utilisateur"),
    SCRUM_MASTER("Scrum Master"),
    PRODUCT_OWNER("Product Owner");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}