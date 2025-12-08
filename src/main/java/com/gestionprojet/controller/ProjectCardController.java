package com.gestionprojet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.gestionprojet.model.Project;

public class ProjectCardController {

    @FXML
    private Label projectNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label startDateLabel;

    @FXML
    private Label endDateLabel;
    @FXML
    private Label statusLabel;

    private Project currentProject;
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void loadProject(Project project) {
        this.currentProject = project;
        projectNameLabel.setText(project.getName());
        descriptionLabel.setText(project.getDescription());
        startDateLabel.setText(formatDate(project.getStartDate()));
        endDateLabel.setText(formatDate(project.getEndDate()));

        updateStatus(project);
    }

    private void updateStatus(Project project) {
        if (project.getEndDate() != null &&
                project.getEndDate().isBefore(java.time.LocalDate.now())) {
            statusLabel.setText("Terminé");
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #64748b; -fx-background-radius: 4px; -fx-padding: 4 8;");
        } else {
            statusLabel.setText("Actif");
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #10b981; -fx-background-radius: 4px; -fx-padding: 4 8;");
        }
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return "Non défini";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }



    @FXML
    private void handleEdit() {
        if (dashboardController != null && currentProject != null) {
            dashboardController.editProject(currentProject);
        }
    }

    @FXML
    private void handleDelete() {
        if (dashboardController != null && currentProject != null) {
            dashboardController.deleteProject(currentProject);
        }
    }

    @FXML
    private void handleManageSprints() {
        if (currentProject == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sprints-view.fxml"));
            Parent root = loader.load();

            SprintsViewController controller = loader.getController();
            controller.setProject(currentProject);
            controller.setDashboardController(dashboardController);

            Stage stage = new Stage();
            stage.setTitle("Gestion des Sprints - " + currentProject.getName());
            stage.setScene(new Scene(root));
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur ouverture vue sprints: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
