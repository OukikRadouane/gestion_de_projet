package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.service.AuthService;
import com.gestionprojet.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserManagementController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> createdAtColumn;
    @FXML private Button backButton;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button toggleStatusButton;
    @FXML private Button hideUserButton;
    @FXML private Button deleteUserButton;
    @FXML private CheckBox showHiddenUsersCheckBox;
    @FXML private ComboBox<String> statusFilterComboBox;

    private UserService userService;
    private AuthService authService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    public void setServices(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
        setupPermissions();
        loadUsers();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        setupTableSelection();
    }

    private void setupTableColumns() {
        firstNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLastName()));
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRole().getLabel()));
        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = user.isActive() ? "Actif" : "Inactif";
            if (user.isHidden()) status += " (Masqué)";
            return new SimpleStringProperty(status);
        });
        createdAtColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        usersTable.setItems(usersList);
    }

    private void setupStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "Tous", "Actifs", "Inactifs"
        ));
        statusFilterComboBox.setValue("Tous");
        statusFilterComboBox.setOnAction(e -> loadUsers());
    }

    private void setupTableSelection() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editUserButton.setDisable(!hasSelection);
            toggleStatusButton.setDisable(!hasSelection);
            hideUserButton.setDisable(!hasSelection);
            deleteUserButton.setDisable(!hasSelection || !userService.canDeletePermanently(authService.getCurrentUser()));
        });
    }

    private void setupPermissions() {
        User currentUser = authService.getCurrentUser();
        boolean canManage = userService.canManageUsers(currentUser);
        boolean canViewHidden = userService.canViewHiddenUsers(currentUser);

        addUserButton.setDisable(!canManage);
        showHiddenUsersCheckBox.setVisible(canViewHidden);
        showHiddenUsersCheckBox.setOnAction(e -> loadUsers());
    }

    private void loadUsers() {
        try {
            List<User> users;
            String statusFilter = statusFilterComboBox.getValue();
            boolean showHidden = showHiddenUsersCheckBox.isSelected();

            if (showHidden && userService.canViewHiddenUsers(authService.getCurrentUser())) {
                users = userService.getAllUsersIncludingHidden();
            } else {
                switch (statusFilter) {
                    case "Actifs":
                        users = userService.getActiveUsers();
                        break;
                    case "Inactifs":
                        users = userService.getInactiveUsers();
                        break;
                    default:
                        users = userService.getAllUsers();
                        break;
                }
            }

            usersList.clear();
            usersList.addAll(users);
        } catch (Exception e) {
            showError("Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        openUserForm(null);
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            openUserForm(selectedUser);
        }
    }

    @FXML
    private void handleToggleStatus() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                userService.toggleUserStatus(selectedUser);
                loadUsers();
                showSuccess("Statut de l'utilisateur modifié avec succès");
            } catch (Exception e) {
                showError("Erreur lors de la modification du statut: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleHideUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setHeaderText("Masquer l'utilisateur");
            confirmDialog.setContentText("Êtes-vous sûr de vouloir masquer cet utilisateur?");

            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    userService.hideUser(selectedUser);
                    loadUsers();
                    showSuccess("Utilisateur masqué avec succès");
                } catch (Exception e) {
                    showError("Erreur lors du masquage: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null && userService.canDeletePermanently(authService.getCurrentUser())) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setHeaderText("Suppression définitive");
            confirmDialog.setContentText("ATTENTION: Cette action est irréversible!\nÊtes-vous sûr de vouloir supprimer définitivement cet utilisateur?");

            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    userService.deleteUserPermanently(selectedUser);
                    loadUsers();
                    showSuccess("Utilisateur supprimé définitivement");
                } catch (Exception e) {
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        }
    }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user-form.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setServices(userService, authService);
            controller.setUser(user);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle(user == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    public void refreshUsers() {
        loadUsers();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/super-admin-dashboard.fxml"));
            Parent root = loader.load();
            
            SuperAdminDashboardController controller = loader.getController();
            controller.setAuthService(authService);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            showError("Erreur lors du retour: " + e.getMessage());
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