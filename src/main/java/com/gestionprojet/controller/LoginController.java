package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            System.out.println("Tentative de connexion avec: " + username);
            authService.login(username, password);
            System.out.println("Connexion réussie, redirection...");
            redirectToDashboard();
        } catch (Exception e) {
            System.err.println("Erreur connexion: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (Exception e) {
            errorLabel.setText("Erreur lors du chargement de la page d'inscription");
            errorLabel.setVisible(true);
        }
    }

    private void redirectToDashboard() {
        try {
            User currentUser = authService.getCurrentUser();
            System.out.println("Redirection utilisateur: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
            
            String fxmlPath;
            String title;

            switch (currentUser.getRole()) {
                case SUPER_ADMIN:
                    fxmlPath = "/view/super-admin-dashboard.fxml";
                    title = "Tableau de bord Super Admin";
                    break;
                case ADMIN:
                    fxmlPath = "/view/dashboard.fxml";
                    title = "Tableau de bord Admin";
                    break;
                default:
                    fxmlPath = "/view/dashboard.fxml";
                    title = "Tableau de bord";
                    break;
            }

            System.out.println("Chargement de: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (currentUser.getRole() == com.gestionprojet.model.enums.Role.SUPER_ADMIN) {
                SuperAdminDashboardController controller = loader.getController();
                controller.setAuthService(authService);
            } else {
                DashboardController controller = loader.getController();
                controller.setAuthService(authService);
                controller.setProjects(authService.getAllProjectsOfCurrentUser());
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            System.out.println("Redirection réussie vers: " + title);
        } catch (Exception e) {
            System.err.println("Erreur redirection: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement du tableau de bord: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}