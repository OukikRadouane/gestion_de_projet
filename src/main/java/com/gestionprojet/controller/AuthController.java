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
    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private TextField loginPasswordFieldVisible;
    @FXML
    private CheckBox showPasswordCheckBox;
    @FXML
    private Label loginErrorLabel;

    @FXML
    private TextField registerUsernameField;
    @FXML
    private TextField registerEmailField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private PasswordField registerConfirmPasswordField;
    @FXML
    private ComboBox<com.gestionprojet.model.enums.Role> registerRoleComboBox;
    @FXML
    private Label registerErrorLabel;

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
        String password = showPasswordCheckBox.isSelected() ? loginPasswordFieldVisible.getText()
                : loginPasswordField.getText();

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            showLoginError("Veuillez saisir votre nom d'utilisateur ou email");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            showLoginError("Veuillez saisir votre mot de passe");
            return;
        }

        try {
            authService.login(usernameOrEmail, password);
            showDashboard();
        } catch (IllegalArgumentException e) {
            showLoginError(e.getMessage());
        } catch (Exception e) {
            showLoginError("Nom d'utilisateur ou mot de passe incorrect");
        }
    }

    @FXML
    private void handleRegister() {
        if (authService == null) {
            showRegisterError("Service d'authentification non disponible");
            return;
        }

        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();
        com.gestionprojet.model.enums.Role role = registerRoleComboBox.getValue();

        if (username == null || username.trim().isEmpty()) {
            showRegisterError("Le nom d'utilisateur est requis");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            showRegisterError("L'adresse email est requise");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            showRegisterError("Le mot de passe est requis");
            return;
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            showRegisterError("Veuillez confirmer votre mot de passe");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showRegisterError("Les mots de passe ne correspondent pas");
            return;
        }

        try {
            authService.register(username, email, password, confirmPassword, role);
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
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("ProjectHub - Inscription");
        } catch (IOException e) {
            showLoginError("Erreur lors du chargement de la vue d'inscription");
        }
    }

    @FXML
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) registerUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("ProjectHub - Connexion");
        } catch (IOException e) {
            showRegisterError("Erreur lors du chargement de la vue de connexion");
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            loginPasswordFieldVisible.setText(loginPasswordField.getText());
            loginPasswordFieldVisible.setVisible(true);
            loginPasswordField.setVisible(false);
        } else {
            loginPasswordField.setText(loginPasswordFieldVisible.getText());
            loginPasswordField.setVisible(true);
            loginPasswordFieldVisible.setVisible(false);
        }
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
            Parent root = loader.load();

            com.gestionprojet.controller.MainController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("ProjectHub - Gestion de Projet");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showLoginError("Erreur lors du chargement de l'application principale");
        }
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setManaged(true);
        }
    }

    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
            registerErrorLabel.setManaged(true);
        }
    }

    public void handleForgotPassword() {
        loginErrorLabel.setText("Fonctionnalité de réinitialisation de mot de passe à implémenter");
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}