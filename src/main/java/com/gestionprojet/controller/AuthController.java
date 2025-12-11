package com.gestionprojet.controller;

import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    @FXML private TextField registerFirstNameField;
    @FXML private TextField registerLastNameField;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private ComboBox<com.gestionprojet.model.enums.Role> registerRoleComboBox;
    @FXML private Label registerErrorLabel;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        if (registerRoleComboBox != null) {
            registerRoleComboBox.getItems().addAll(com.gestionprojet.model.enums.Role.values());
            registerRoleComboBox.setValue(com.gestionprojet.model.enums.Role.USER);
        }
    }

    @FXML
    private void handleLogin() {
        if (authService == null) {
            showLoginError("Service d'authentification non disponible");
            return;
        }

        String usernameOrEmail = loginUsernameField.getText();
        String password = loginPasswordField.getText();

        try {

            authService.login(usernameOrEmail, password);
            showDashboard();
        } catch (IllegalArgumentException e) {
            showLoginError(e.getMessage());
        } catch (Exception e) {
            showLoginError("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        if (authService == null) {
            showRegisterError("Service d'authentification non disponible");
            return;
        }

        String firstName = registerFirstNameField.getText();
        String lastName = registerLastNameField.getText();
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();
        com.gestionprojet.model.enums.Role role = registerRoleComboBox.getValue();

        try {

            authService.register(firstName, lastName, username, email, password, confirmPassword, role);
            showSuccess("Inscription réussie! Vous pouvez maintenant vous connecter.");
            showLoginView();
        } catch (IllegalArgumentException e) {
            showRegisterError(e.getMessage());
        } catch (Exception e) {
            showRegisterError("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    @FXML
    private void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showLoginError("Erreur lors du chargement de la vue d'inscription");
        }
    }

    @FXML
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) registerUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showRegisterError("Erreur lors du chargement de la vue de connexion");
        }
    }

    private void showDashboard() {
        try {
            com.gestionprojet.model.User currentUser = authService.getCurrentUser();
            String fxmlPath;
            String title;
            
            if (currentUser.getRole() == com.gestionprojet.model.enums.Role.SUPER_ADMIN) {
                fxmlPath = "/view/super-admin-dashboard.fxml";
                title = "Tableau de bord Super Admin";
            } else {
                fxmlPath = "/view/dashboard.fxml";
                title = "Project Manager - Dashboard";
            }
            
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
            
            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle(title);
        } catch (IOException e) {
            showLoginError("Erreur lors du chargement du tableau de bord");
        }
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
        }
    }

    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
        }
    }
    public void handleForgotPassword() {
        loginErrorLabel.setText("Fonctionnalité de réinitialisation de mot de passe à implémenter");
        loginErrorLabel.setVisible(true);
    }
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}