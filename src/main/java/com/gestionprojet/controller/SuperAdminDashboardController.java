package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SuperAdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    
    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        loadUserInfo();
    }

    @FXML
    public void initialize() {
        System.out.println("SuperAdminDashboardController initialisé");
    }

    private void loadUserInfo() {
        if (authService != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            welcomeLabel.setText("Bienvenue " + user.getUsername());
            roleLabel.setText(user.getRole().toString());
        }
    }

    @FXML
    private void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user-management.fxml"));
            Parent root = loader.load();

            UserManagementController controller = loader.getController();
            controller.setServices(
                new com.gestionprojet.service.UserService(new com.gestionprojet.dao.UserDAO()),
                authService
            );

            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Gestion des Utilisateurs");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la gestion des utilisateurs");
        }
    }

    @FXML
    private void handleProjectManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setProjects(authService.getAllProjectsOfCurrentUser());

            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Gestion des Projets");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la gestion des projets");
        }
    }

    @FXML
    private void handleReports() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapports et Statistiques");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("Les rapports et statistiques seront disponibles prochainement.");
        alert.showAndWait();
    }

    @FXML
    private void handleSystemConfig() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuration Système");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("La configuration système sera disponible prochainement.");
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        // Le super admin est déjà sur sa page principale, donc pas de retour
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Page principale");
        alert.setContentText("Vous êtes déjà sur la page principale.");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        if (authService != null) {
            authService.logout();
        }
        showLoginView();
    }

    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            
            LoginController controller = loader.getController();
            controller.setAuthService(authService);
            
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Connexion");
        } catch (Exception e) {
            System.err.println("Erreur retour à la connexion: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}