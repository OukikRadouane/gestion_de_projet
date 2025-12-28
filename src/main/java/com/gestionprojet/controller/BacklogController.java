package com.gestionprojet.controller;

import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.dao.TaskDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BacklogController {

    @FXML
    private TableView<Task> backlogTable;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colDeadline;
    @FXML
    private TableColumn<Task, String> colAssignee;
    @FXML
    private TableColumn<Task, Task> colActions;

    private final TaskDAO taskDAO = new TaskDAO();
    private final SprintDAO sprintDAO = new SprintDAO();
    private Project project;
    private User currentUser;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        colPriority.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPriority() != null ? cellData.getValue().getPriority().getDisplayName()
                        : "N/A"));
        colDeadline.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDeadline() != null ? cellData.getValue().getDeadline().toString() : "N/A"));
        colAssignee.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAssignee() != null ? cellData.getValue().getAssignee().getUsername()
                        : "Non assigné"));
        colActions.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnPlanify = new Button("Planifier");
            private final Button btnDetails = new Button("Détails");
            private final HBox container = new HBox(8, btnPlanify, btnDetails);
            {
                btnPlanify.getStyleClass().add("button-primary");
                btnPlanify.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                btnPlanify.setOnAction(event -> {
                    Task task = getTableRow().getItem();
                    if (task != null)
                        handlePlanify(task);
                });

                btnDetails.getStyleClass().add("button-outline");
                btnDetails.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                btnDetails.setOnAction(event -> {
                    Task task = getTableRow().getItem();
                    if (task != null)
                        handleDetails(task);
                });
            }

            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void handlePlanify(Task task) {
        if (project == null)
            return;

        List<Sprint> projectSprints = sprintDAO.getAllSprintsByProject(project);
        List<Sprint> activeSprints = projectSprints.stream()
                .filter(s -> s.getStatus() != com.gestionprojet.model.enums.SprintStatus.COMPLETED)
                .collect(Collectors.toList());

        if (activeSprints.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucun sprint actif ou planifié disponible.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Sprint> dialog = new ChoiceDialog<>(activeSprints.get(0), activeSprints);
        dialog.setTitle("Planifier la tâche");
        dialog.setHeaderText("Choisir un sprint pour : " + task.getTitle());
        dialog.setContentText("Sprint :");

        dialog.showAndWait().ifPresent(sprint -> {
            try {
                task.setSprint(sprint);
                task.setStatus(com.gestionprojet.model.Tasks.TaskStatus.TO_DO);
                task.setPriority(com.gestionprojet.model.Tasks.Priority.HIGH); // Priorité élevée automatique
                task.addLog("Tâche planifiée dans le sprint : " + sprint.getName() + " (Priorité mise à HIGH)",
                        currentUser);
                taskDAO.update(task);
                loadBacklog(); // Rafraîchir la table
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        loadBacklog();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadBacklog() {
        if (project == null)
            return;
        List<Task> backlogTasks = taskDAO.getBacklogByProject(project);
        backlogTable.setItems(FXCollections.observableArrayList(backlogTasks));
    }

    @FXML
    private void handleAddTask() {
        if (project == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent rootNode = loader.load();

            TaskDialogController controller = loader.getController();
            controller.setProject(project);
            controller.setSprint(null);
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            Scene scene = new Scene(rootNode);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            // Centrer la fenêtre
            stage.setOnShown(e -> {
                Stage owner = (Stage) backlogTable.getScene().getWindow();
                if (owner != null) {
                    stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                    stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
                }
            });

            // Déplacement
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];
            rootNode.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            rootNode.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            });

            stage.showAndWait();
            loadBacklog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDetails(Task task) {
        try {
            Task fullTask = taskDAO.getByIdWithCollections(task.getId());
            if (fullTask == null)
                return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDetailsView.fxml"));
            Parent root = loader.load();

            TaskDetailsController controller = loader.getController();
            controller.setTask(fullTask);
            controller.setCurrentUser(this.currentUser);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            Scene scene = new Scene(root, 900, 800);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            // Centrer la fenêtre
            stage.setOnShown(e -> {
                Stage owner = (Stage) backlogTable.getScene().getWindow();
                if (owner != null) {
                    stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                    stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
                }
            });

            // Déplacement
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];
            root.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
