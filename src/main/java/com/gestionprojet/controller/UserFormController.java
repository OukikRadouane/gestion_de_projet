package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.service.AuthService;
import com.gestionprojet.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserFormController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;

    private UserService userService;
    private AuthService authService;
    private User currentUser;
    private UserManagementController parentController;

    public void setServices(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
        setupRoleComboBox();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            loadUserData();
            setupEditMode();
        } else {
            setupCreateMode();
        }
    }

    public void setParentController(UserManagementController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        setupValidation();
    }

    private void setupRoleComboBox() {
        User loggedUser = authService.getCurrentUser();
        if (loggedUser.getRole() == Role.SUPER_ADMIN) {
            roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        } else {
            // Les admins ne peuvent pas créer de super admins
            roleComboBox.setItems(FXCollections.observableArrayList(
                Role.ADMIN, Role.USER, Role.SCRUM_MASTER, Role.PRODUCT_OWNER
            ));
        }
        roleComboBox.setValue(Role.USER);
    }

    private void loadUserData() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());
        roleComboBox.setValue(currentUser.getRole());
        activeCheckBox.setSelected(currentUser.isActive());
    }

    private void setupEditMode() {
        usernameField.setDisable(true); // Ne pas permettre de changer le username
        passwordField.setVisible(false);
        confirmPasswordField.setVisible(false);
        passwordLabel.setVisible(false);
        confirmPasswordLabel.setVisible(false);
        activeCheckBox.setVisible(true);
    }

    private void setupCreateMode() {
        activeCheckBox.setSelected(true);
        activeCheckBox.setVisible(false); // Nouveaux utilisateurs sont actifs par défaut
    }

    private void setupValidation() {
        // Validation en temps réel
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        boolean isValid = true;
        
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            usernameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty()) {
            isValid = false;
        }

        if (currentUser == null) { // Mode création
            if (passwordField.getText().length() < 6 ||
                !passwordField.getText().equals(confirmPasswordField.getText())) {
                isValid = false;
            }
        }

        saveButton.setDisable(!isValid);
    }

    @FXML
    private void handleSave() {
        try {
            if (currentUser == null) {
                // Création d'un nouvel utilisateur
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showError("Les mots de passe ne correspondent pas");
                    return;
                }

                userService.createUser(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    roleComboBox.getValue()
                );
                showSuccess("Utilisateur créé avec succès");
            } else {
                // Modification d'un utilisateur existant
                userService.updateUser(
                    currentUser,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    roleComboBox.getValue()
                );

                // Mise à jour du statut actif/inactif
                if (currentUser.isActive() != activeCheckBox.isSelected()) {
                    userService.toggleUserStatus(currentUser);
                }

                showSuccess("Utilisateur modifié avec succès");
            }

            if (parentController != null) {
                parentController.refreshUsers();
            }
            closeWindow();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}