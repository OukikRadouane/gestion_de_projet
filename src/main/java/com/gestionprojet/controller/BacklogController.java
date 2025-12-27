package com.gestionprojet.controller;

import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.dao.TaskDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Task;
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
    private TableColumn<Task, Void> colActions;

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
                    Task task = getTableView().getItems().get(getIndex());
                    handlePlanify(task);
                });

                btnDetails.getStyleClass().add("button-outline");
                btnDetails.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                btnDetails.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    handleDetails(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
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
        List<Sprint> sprints = sprintDAO.getAllSprintsByProject(project);
        if (sprints.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucun sprint disponible pour ce projet.");
            alert.show();
            return;
        }

        ChoiceDialog<Sprint> dialog = new ChoiceDialog<>(sprints.get(0), sprints);
        dialog.setTitle("Planifier la tâche");
        dialog.setHeaderText("Choisir un sprint pour : " + task.getTitle());
        dialog.setContentText("Sprint :");

        dialog.showAndWait().ifPresent(sprint -> {
            task.setSprint(sprint);
            task.setStatus(com.gestionprojet.model.Tasks.TaskStatus.TO_DO);
            task.addLog("Tâche planifiée dans le sprint : " + sprint.getName(), currentUser);
            taskDAO.update(task);
            loadBacklog();
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
        List<Task> allProjectTasks = taskDAO.getByProject(project);
        // Filtre : Tâches qui n'appartiennent à aucun sprint (Backlog Produit)
        List<Task> backlogTasks = allProjectTasks.stream()
                .filter(t -> t.getSprint() == null)
                .collect(Collectors.toList());

        backlogTable.setItems(FXCollections.observableArrayList(backlogTasks));
    }

    @FXML
    private void handleAddTask() {
        if (project == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent root = loader.load();

            TaskDialogController controller = loader.getController();
            controller.setProject(project);
            controller.setSprint(null);
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Tâche au Backlog");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBacklog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDetails(Task task) {
        // Logic for details (can reuse TaskDetailsController)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDetailsView.fxml"));
            Parent root = loader.load();

            TaskDetailsController controller = loader.getController();
            controller.setTask(task);
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Détails de la tâche - " + task.getTitle());
            stage.setScene(new Scene(root, 900, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
