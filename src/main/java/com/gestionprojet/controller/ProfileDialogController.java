package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProfileDialogController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private AuthService authService;
    private User currentUser;
    private DashboardController dashboardController;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.currentUser = authService.getCurrentUser();
        loadUserData();
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    private void loadUserData() {
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            usernameField.setEditable(false); // Le nom d'utilisateur ne peut pas être modifié
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleSave() {
        String newEmail = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation de base
        if (newEmail.isEmpty()) {
            showError("L'email ne peut pas être vide");
            return;
        }

        // Si l'utilisateur veut changer son mot de passe
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                showError("Veuillez entrer votre mot de passe actuel pour le changer");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("Les nouveaux mots de passe ne correspondent pas");
                return;
            }

            if (newPassword.length() < 6) {
                showError("Le nouveau mot de passe doit contenir au moins 6 caractères");
                return;
            }
        }

        try {
            // Vérifier le mot de passe actuel si fourni
            if (!currentPassword.isEmpty()) {
                authService.login(currentUser.getUsername(), currentPassword);
            }

            // Mettre à jour le profil
            authService.updateProfile(
                currentUser,
                newEmail,
                newPassword.isEmpty() ? null : newPassword
            );

            // Rafraîchir le dashboard
            if (dashboardController != null) {
                dashboardController.refreshUserInfo();
            }

            showSuccess("Profil mis à jour avec succès");
            handleCancel();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
