package com.gestionprojet.controller;

import com.gestionprojet.controller.kanbanController;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.enums.SprintStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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

    @FXML
    private void handleManageTasks() {
        if (currentSprint == null || currentSprint.getProject() == null) {
            return;
        }

        try {
            // Créer le controller Kanban
            kanbanController kanban = new kanbanController();
            if (sprintsViewController != null) {
                kanban.setUser(sprintsViewController.getCurrentUser());
            }
            BorderPane kanbanView = kanban.createView();
            kanban.setSprint(currentSprint);

            Stage stage = new Stage();
            stage.setTitle("Tableau Kanban - " + currentSprint.getProject().getName());
            stage.setScene(new Scene(kanbanView, 1200, 750));
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur ouverture tableau Kanban: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
