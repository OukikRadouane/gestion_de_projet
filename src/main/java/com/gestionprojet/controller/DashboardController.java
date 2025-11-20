package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label currentRoleLabel;
    
    // Ajoutez ces labels pour les statistiques
    @FXML private Label projectsCountLabel;
    @FXML private Label collaboratorsCountLabel;
    @FXML private Label tasksCountLabel;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        loadUserInfo();
    }

    @FXML
    public void initialize() {
        System.out.println("DashboardController initialisé");
        // Initialiser les statistiques par défaut
        if (projectsCountLabel != null) projectsCountLabel.setText("0");
        if (collaboratorsCountLabel != null) collaboratorsCountLabel.setText("0");
        if (tasksCountLabel != null) tasksCountLabel.setText("0");
    }

    private void loadUserInfo() {
        if (authService != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            System.out.println("Chargement info utilisateur: " + user.getUsername());
            
            if (welcomeLabel != null) welcomeLabel.setText("Bienvenue, " + user.getUsername() + "!");
            if (usernameLabel != null) usernameLabel.setText(user.getUsername());
            if (emailLabel != null) emailLabel.setText(user.getEmail());
            if (roleLabel != null) roleLabel.setText(user.getRole().toString());
            if (currentUserLabel != null) currentUserLabel.setText(user.getUsername());
            if (currentRoleLabel != null) currentRoleLabel.setText(user.getRole().toString());
        } else {
            System.out.println("Utilisateur non connecté");
            if (welcomeLabel != null) welcomeLabel.setText("Bienvenue dans votre tableau de bord");
            if (usernameLabel != null) usernameLabel.setText("Utilisateur");
            if (emailLabel != null) emailLabel.setText("email@exemple.com");
            if (roleLabel != null) roleLabel.setText("Utilisateur");
            if (currentUserLabel != null) currentUserLabel.setText("Utilisateur");
            if (currentRoleLabel != null) currentRoleLabel.setText("Utilisateur");
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion...");
        if (authService != null) {
            authService.logout();
        }
        showLoginView();
    }

    @FXML
    private void handleEditProfile() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Édition de profil");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("La modification de profil sera disponible prochainement.");
        alert.showAndWait();
    }

    @FXML
    private void handleCreateProject() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Création de projet");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("La création de projet sera disponible prochainement.");
        alert.showAndWait();
    }

    private void showLoginView() {
        try {
            System.out.println("Retour à la vue login...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            if (authService != null) {
                controller.setAuthService(authService);
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Project Manager - Connexion");
            // Redimensionner la fenêtre pour la vue login
            stage.setWidth(900);
            stage.setHeight(650);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erreur retour login: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la déconnexion");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
