package com.gestionprojet.controller;

import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class AuthController {
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label loginErrorLabel;

    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private ComboBox<com.gestionprojet.model.enums.Role> registerRoleComboBox;
    @FXML private CheckBox termsCheckBox;
    @FXML private Label registerErrorLabel;

    private AuthService authService;
    private Preferences prefs = Preferences.userNodeForPackage(AuthController.class);

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        if (registerRoleComboBox != null) {
            registerRoleComboBox.getItems().addAll(com.gestionprojet.model.enums.Role.values());
            registerRoleComboBox.setValue(com.gestionprojet.model.enums.Role.USER);
        }
        
        // Charger uniquement le nom d'utilisateur/email sauvegardé
        if (loginUsernameField != null && rememberMeCheckBox != null) {
            String savedUsername = prefs.get("savedUsername", "");
            boolean rememberMe = prefs.getBoolean("rememberMe", false);
            
            if (rememberMe && !savedUsername.isEmpty()) {
                loginUsernameField.setText(savedUsername);
                rememberMeCheckBox.setSelected(true);
            }
        }
    }

    @FXML
    private void handleLogin() {
        if (authService == null) {
            showLoginError("Service d'authentification non disponible");
            return;
        }

        String usernameOrEmail = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        // Validation des champs
        if (usernameOrEmail.isEmpty()) {
            showLoginError("Veuillez entrer votre nom d'utilisateur ou email");
            return;
        }

        if (password.isEmpty()) {
            showLoginError("Veuillez entrer votre mot de passe");
            return;
        }

        try {
            authService.login(usernameOrEmail, password);
            
            // Sauvegarder uniquement le nom d'utilisateur/email si "Se souvenir de moi" est coché
            if (rememberMeCheckBox.isSelected()) {
                prefs.put("savedUsername", usernameOrEmail);
                prefs.putBoolean("rememberMe", true);
            } else {
                prefs.remove("savedUsername");
                prefs.putBoolean("rememberMe", false);
            }
            
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

        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();
        com.gestionprojet.model.enums.Role role = registerRoleComboBox.getValue();

        // Validation des champs vides
        if (username.isEmpty()) {
            showRegisterError("Le nom d'utilisateur est obligatoire");
            return;
        }

        if (email.isEmpty()) {
            showRegisterError("L'adresse email est obligatoire");
            return;
        }

        if (password.isEmpty()) {
            showRegisterError("Le mot de passe est obligatoire");
            return;
        }

        if (confirmPassword.isEmpty()) {
            showRegisterError("Veuillez confirmer votre mot de passe");
            return;
        }

        // Validation de la longueur du mot de passe
        if (password.length() < 8) {
            showRegisterError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        // Vérifier que les conditions sont acceptées
        if (!termsCheckBox.isSelected()) {
            showRegisterError("Vous devez accepter les conditions d'utilisation");
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

            AuthController controller = loader.getController();
            controller.setAuthService(authService);

            Stage stage = (Stage) registerUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showRegisterError("Erreur lors du chargement de la vue de connexion");
        }
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();

            com.gestionprojet.controller.DashboardController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setProjects(authService.getAllProjectsOfCurrentUser());

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Project Manager - Dashboard");
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}