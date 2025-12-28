package com.gestionprojet.controller;

import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.model.enums.SprintStatus;
import com.gestionprojet.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SprintCardController {

    @FXML
    private Label sprintNameLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label endDateLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label goalLabel;
    @FXML
    private Button btnEditSprint;
    @FXML
    private Button btnDeleteSprint;

    private Sprint currentSprint;
    private SprintsViewController sprintsViewController;

    public void setSprintsViewController(SprintsViewController controller) {
        this.sprintsViewController = controller;
    }

    public void loadSprint(Sprint sprint) {
        this.currentSprint = sprint;
        sprintNameLabel.setText(sprint.getName());
        startDateLabel.setText(formatDate(sprint.getStartDate()));
        endDateLabel.setText(formatDate(sprint.getEndDate()));

        long days = sprint.getDurationInDays();
        durationLabel.setText(days + " jour(s)");

        if (sprint.getGoal() != null && !sprint.getGoal().trim().isEmpty()) {
            goalLabel.setText(sprint.getGoal());
        } else {
            goalLabel.setText("Aucun objectif défini");
            goalLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
        }

        updateStatus(sprint);

        // RBAC logic for buttons
        Role role = SessionManager.getInstance().getCurrentUser().getRole();
        boolean canManage = (role == Role.ADMIN || role == Role.SCRUM_MASTER);
        if (btnEditSprint != null) {
            btnEditSprint.setVisible(canManage);
            btnEditSprint.setManaged(canManage);
        }
        if (btnDeleteSprint != null) {
            btnDeleteSprint.setVisible(canManage);
            btnDeleteSprint.setManaged(canManage);
        }
    }

    private void updateStatus(Sprint sprint) {
        SprintStatus status = sprint.getStatus();
        String statusText = status.getLabel();

        // Mettre à jour le statut si nécessaire (sprint actif ou terminé basé sur les
        // dates)
        if (sprint.isActive() && status != SprintStatus.ACTIVE) {
            statusText = "Actif";
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                    "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #10b981; " +
                    "-fx-background-radius: 4px; -fx-padding: 4 8;");
        } else if (sprint.isCompleted() && status != SprintStatus.COMPLETED && status != SprintStatus.CANCELLED) {
            statusText = "Terminé";
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                    "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #64748b; " +
                    "-fx-background-radius: 4px; -fx-padding: 4 8;");
        } else {
            switch (status) {
                case PLANNED:
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                            "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #3b82f6; " +
                            "-fx-background-radius: 4px; -fx-padding: 4 8;");
                    break;
                case ACTIVE:
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                            "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #10b981; " +
                            "-fx-background-radius: 4px; -fx-padding: 4 8;");
                    break;
                case COMPLETED:
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                            "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #64748b; " +
                            "-fx-background-radius: 4px; -fx-padding: 4 8;");
                    break;
                case CANCELLED:
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; " +
                            "-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #ef4444; " +
                            "-fx-background-radius: 4px; -fx-padding: 4 8;");
                    break;
            }
        }

        statusLabel.setText(statusText);
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null)
            return "Non défini";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML
    private void handleViewTasks() {
        if (sprintsViewController != null && currentSprint != null) {
            sprintsViewController.navigateToKanban(currentSprint);
        }
    }

    @FXML
    private void handleEdit() {
        if (sprintsViewController != null && currentSprint != null) {
            sprintsViewController.editSprint(currentSprint);
        }
    }

    @FXML
    private void handleDelete() {
        if (sprintsViewController != null && currentSprint != null) {
            sprintsViewController.deleteSprint(currentSprint);
        }
    }

}
