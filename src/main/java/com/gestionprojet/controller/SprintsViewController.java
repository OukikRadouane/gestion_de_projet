package com.gestionprojet.controller;

import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SprintsViewController {

    @FXML
    private Label projectNameLabel;
    @FXML
    private GridPane sprintsGrid;
    @FXML
    private VBox emptyStateContainer;

    private SprintDAO sprintDAO = new SprintDAO();
    private Project project;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null && projectNameLabel != null) {
            projectNameLabel.setText("Sprints du projet: " + project.getName());
        }
        loadSprints();
    }

    private com.gestionprojet.model.User currentUser;

    public void setCurrentUser(com.gestionprojet.model.User user) {
        this.currentUser = user;
    }

    public com.gestionprojet.model.User getCurrentUser() {
        return this.currentUser;
    }

    public void loadSprints() {
        if (sprintsGrid == null || project == null) {
            return;
        }

        sprintsGrid.getChildren().clear();

        List<Sprint> sprints = sprintDAO.getAllSprintsByProject(project);

        if (sprints == null || sprints.isEmpty()) {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);
                sprintsGrid.add(emptyStateContainer, 0, 0);
                GridPane.setColumnSpan(emptyStateContainer, 2);
            }
        } else {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(false);
                emptyStateContainer.setManaged(false);
            }

            int row = 0;
            int col = 0;

            for (Sprint sprint : sprints) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sprint-card.fxml"));
                    VBox sprintCard = loader.load();

                    SprintCardController cardController = loader.getController();
                    cardController.loadSprint(sprint);
                    cardController.setSprintsViewController(this);

                    sprintsGrid.add(sprintCard, col, row);
                    GridPane.setHgrow(sprintCard, Priority.ALWAYS);

                    col++;
                    if (col >= 2) {
                        col = 0;
                        row++;
                    }
                } catch (IOException e) {
                    System.err.println("Erreur chargement carte sprint: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void refreshSprints() {
        loadSprints();
    }

    public void navigateToKanban(Sprint sprint) {
        if (mainController != null) {
            mainController.selectSprint(sprint);
            mainController.showKanban();
        }
    }

    @FXML
    private void handleAddSprint() {
        openSprintForm(null);
    }

    private void openSprintForm(Sprint sprint) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sprint.fxml"));
            Parent root = loader.load();

            SprintController controller = loader.getController();
            controller.setSprint(sprint);
            controller.setProject(project);
            controller.setSprintsViewController(this);

            Stage stage = new Stage();
            stage.setTitle(sprint == null ? "Nouveau Sprint" : "Modifier Sprint");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setWidth(500);
            stage.setHeight(550);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("Erreur ouverture formulaire sprint: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editSprint(Sprint sprint) {
        openSprintForm(sprint);
    }

    public void deleteSprint(Sprint sprint) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr?");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer le sprint \"" + sprint.getName() + "\"?\n" +
                "Les tâches associées ne seront pas supprimées mais ne seront plus associées à un sprint.");

        if (confirmDialog.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            try {
                sprintDAO.delete(sprint);
                loadSprints();
                showSuccess("Sprint supprimé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur suppression: " + e.getMessage());
                showError("Erreur lors de la suppression du sprint");
            }
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showDashboard();
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
