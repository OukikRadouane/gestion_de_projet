package com.gestionprojet.controller;

import com.gestionprojet.dao.UserDAO;
import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import com.gestionprojet.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Optional;

public class ProfileController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField roleField;
    @FXML
    private Label messageLabel;

    private UserDAO userDAO = new UserDAO();
    private AuthService authService;
    private MainController mainController;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        loadUserData();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void loadUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            roleField.setText(currentUser.getRole().toString());
        }
    }

    @FXML
    private void handleSave() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            currentUser.setUsername(newUsername);
            currentUser.setEmail(newEmail);
            userDAO.save(currentUser);

            showSuccess("Profil mis à jour avec succès.");
            if (mainController != null) {
                // Refresh sidebar info if needed
                mainController.setAuthService(authService);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la mise à jour du profil.");
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer définitivement le compte ?");
        alert.setContentText("Cette action est irréversible. Toutes vos données seront perdues.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                userDAO.delete(currentUser);
                if (mainController != null) {
                    mainController.handleLogout();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression du compte.");
            }
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 600;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 600;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
