package com.gestionprojet.controller;

import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Priority;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class DashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label currentUserLabel;
    @FXML
    private Label currentRoleLabel;
    @FXML
    private Label projectsCountLabel;
    @FXML
    private Label collaboratorsCountLabel;
    @FXML
    private Label tasksCountLabel;
    @FXML
    private GridPane projectsGrid;
    @FXML
    private VBox emptyStateContainer;

    private AuthService authService;
    private List<Project> projects;
    private ProjectDAO projectDAO = new ProjectDAO();

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        loadUserInfo();
    }

    public User getCurrentUser() {
        return authService != null ? authService.getCurrentUser() : null;
    }

    @FXML
    public void initialize() {
        System.out.println("DashboardController initialisé");
        if (projectsCountLabel != null)
            projectsCountLabel.setText("0");
        if (collaboratorsCountLabel != null)
            collaboratorsCountLabel.setText("0");
        if (tasksCountLabel != null)
            tasksCountLabel.setText("0");
    }

    private void loadUserInfo() {
        if (authService != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            System.out.println("Chargement info utilisateur: " + user.getUsername());

            if (welcomeLabel != null)
                welcomeLabel.setText("Bienvenue, " + user.getUsername() + "!");
            if (usernameLabel != null)
                usernameLabel.setText(user.getUsername());
            if (emailLabel != null)
                emailLabel.setText(user.getEmail());
            if (roleLabel != null)
                roleLabel.setText(user.getRole().toString());
            if (currentUserLabel != null)
                currentUserLabel.setText(user.getUsername());
            if (currentRoleLabel != null)
                currentRoleLabel.setText(user.getRole().toString());
        } else {
            System.out.println("Utilisateur non connecté");
            if (welcomeLabel != null)
                welcomeLabel.setText("Bienvenue dans votre tableau de bord");
            if (usernameLabel != null)
                usernameLabel.setText("Utilisateur");
            if (emailLabel != null)
                emailLabel.setText("email@exemple.com");
            if (roleLabel != null)
                roleLabel.setText("Utilisateur");
            if (currentUserLabel != null)
                currentUserLabel.setText("Utilisateur");
            if (currentRoleLabel != null)
                currentRoleLabel.setText("Utilisateur");
        }
        loadProjects();
    }

    public void loadProjects() {
        if (projectsGrid == null) {
            System.err.println("projectsGrid est null");
            return;
        }

        projectsGrid.getChildren().clear();

        if (projects == null || projects.isEmpty()) {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);
                projectsGrid.add(emptyStateContainer, 0, 0);
                GridPane.setColumnSpan(emptyStateContainer, 2);
            }
            if (projectsCountLabel != null)
                projectsCountLabel.setText("0");
        } else {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(false);
                emptyStateContainer.setManaged(false);
            }

            int row = 0;
            int col = 0;

            for (Project project : projects) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/project-card.fxml"));
                    VBox projectCard = loader.load();

                    ProjectCardController cardController = loader.getController();
                    cardController.loadProject(project);
                    cardController.setDashboardController(this);

                    projectsGrid.add(projectCard, col, row);
                    GridPane.setHgrow(projectCard, Priority.ALWAYS);

                    col++;
                    if (col >= 2) {
                        col = 0;
                        row++;
                    }
                } catch (IOException e) {
                    System.err.println("Erreur chargement carte projet: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (projectsCountLabel != null)
                projectsCountLabel.setText(String.valueOf(projects.size()));
        }
    }

    public void setProjects(List<Project> projectList) {
        this.projects = projectList;
        loadProjects();
    }

    public void refreshProjects(List<Project> projectList) {
        this.projects = projectList;
        loadProjects();
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
    private void AddProject() {
        openProjectForm(null);
    }

    @FXML
    private void handleOpenKanban() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kanban.fxml"));
            // Note: We might need to create kanban.fxml if it doesn't exist, or use the
            // existing way kanban is loaded.
            // Based on previous analysis, kanbanController creates its own view in
            // createView(), but let's check if there is a fxml or if we need to wrap it.
            // Wait, kanbanController has createView() which returns a BorderPane. It seems
            // it's not fully FXML based or the user wants me to use the existing controller
            // logic.
            // Let's look at how SprintsViewController loads sprint-card.fxml.
            // Actually, looking at kanbanController, it has a createView() method that
            // builds the UI programmatically.
            // So I should instantiate the controller and call createView().

            com.gestionprojet.controller.kanbanController controller = new com.gestionprojet.controller.kanbanController();
            if (authService != null) {
                controller.setUser(authService.getCurrentUser());
            }
            // We need to pass the user or something? The controller has setSprint, etc.
            // For the global view, we might need to initialize it differently.

            Parent root = controller.createView();

            Stage stage = new Stage();
            stage.setTitle("Tableau Kanban Global");
            stage.setScene(new Scene(root));
            stage.setWidth(1000);
            stage.setHeight(800);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur ouverture Kanban: " + e.getMessage());
            e.printStackTrace();
            showError("Impossible d'ouvrir le tableau Kanban");
        }
    }

    private void openProjectForm(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/project.fxml"));
            Parent root = loader.load();

            ProjectController controller = loader.getController();
            controller.setProject(project);
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle(project == null ? "Nouveau Projet" : "Modifier Projet");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setWidth(480);
            stage.setHeight(600);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("Erreur ouverture formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editProject(Project project) {
        openProjectForm(project);
    }

    public void deleteProject(Project project) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr?");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer le projet \"" + project.getName() + "\"?");

        if (confirmDialog.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            try {
                projects.remove(project);
                projectDAO.delete(project);
                loadProjects();
                showSuccess("Projet supprimé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur suppression: " + e.getMessage());
                showError("Erreur lors de la suppression du projet");
            }
        }
    }

    private void showLoginView() {
        try {
            System.out.println("Retour à la vue login...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            if (authService != null) {
                controller.setAuthService(authService);
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Project Manager - Connexion");
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
