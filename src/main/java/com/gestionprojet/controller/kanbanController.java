package com.gestionprojet.controller;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskStatus;
import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.dao.TaskDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class kanbanController {

    private VBox todoColumn;
    private VBox doingColumn;
    private VBox doneColumn;
    private Button addTaskButton;
    private BorderPane root;
    private Sprint sprint;
    private User user;
    private Project project;
    private List<Task> tasks;

    private ComboBox<Project> projectCombo;
    private ComboBox<Sprint> sprintCombo;

    private final TaskDAO taskDAO = new TaskDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final SprintDAO sprintDAO = new SprintDAO();

    public kanbanController() {
        this.tasks = new ArrayList<>();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void loadTasksForSprint() {
        if (sprint != null) {
            tasks = taskDAO.getBySprint(sprint);
            System.out.println("Chargement des tâches pour le sprint: " + sprint.getName() + ", nombre de tâches: "
                    + tasks.size());
        } else {
            tasks = new ArrayList<>();
            System.out.println("Aucun sprint défini, liste de tâches vide");
        }

        if (todoColumn != null && doingColumn != null && doneColumn != null) {
            refreshColumns();
        }
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
        System.out.println("Sprint défini dans kanbanController: " + (sprint != null ? sprint.getName() : "null"));
        reloadTasks();
    }

    private void reloadTasks() {
        if (sprint != null) {
            loadTasksForSprint();
        } else if (project != null) {
            loadTasksByProject(project.getId());
        } else {
            tasks = new ArrayList<>();
            refreshColumns();
        }
    }

    public BorderPane createView() {
        root = new BorderPane();
        root.setPrefHeight(600);
        root.setPrefWidth(900);
        root.setStyle("-fx-background-color: #F9FAFB;");
        root.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Create top bar
        root.setTop(createTopBar());

        // Create kanban columns
        root.setCenter(createKanbanColumns());

        setupDropTarget(todoColumn, TaskStatus.TO_DO);
        setupDropTarget(doingColumn, TaskStatus.DOING);
        setupDropTarget(doneColumn, TaskStatus.DONE);

        return root;
    }

    private VBox createTopBar() {
        VBox topBar = new VBox(15);
        topBar.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;");
        topBar.setPadding(new Insets(25, 30, 25, 30));

        Label titleLabel = new Label("Tableau Kanban");
        titleLabel.getStyleClass().add("h1");

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        projectCombo = new ComboBox<>();
        projectCombo.setPromptText("Sélectionner un projet");
        projectCombo.setPrefWidth(220);
        projectCombo.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 6;");

        sprintCombo = new ComboBox<>();
        sprintCombo.setPromptText("Tous les sprints");
        sprintCombo.setPrefWidth(220);
        sprintCombo.setDisable(true);
        sprintCombo.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 6;");

        // Configuration des convertisseurs for project name display
        projectCombo.setConverter(new StringConverter<Project>() {
            @Override
            public String toString(Project project) {
                return project != null ? project.getName() : "";
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        });

        sprintCombo.setConverter(new StringConverter<Sprint>() {
            @Override
            public String toString(Sprint sprint) {
                return sprint != null ? sprint.getName() : "Tous les sprints";
            }

            @Override
            public Sprint fromString(String string) {
                return null;
            }
        });

        loadProjects();

        projectCombo.setOnAction(e -> handleProjectSelection());
        sprintCombo.setOnAction(e -> handleSprintSelection());

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        addTaskButton = new Button("+ Nouvelle Tâche");
        addTaskButton.getStyleClass().add("button-primary");
        addTaskButton.setOnAction(e -> handleAddTask());
        addTaskButton.setVisible(false);

        controls.getChildren().addAll(projectCombo, sprintCombo, spacer, addTaskButton);
        topBar.getChildren().addAll(titleLabel, controls);

        return topBar;
    }

    private HBox createKanbanColumns() {
        HBox columnsContainer = new HBox(25);
        columnsContainer.setAlignment(Pos.TOP_CENTER);
        columnsContainer.setPadding(new Insets(30));

        // Create columns with corresponding status titles
        VBox todoColumnContainer = createColumnContainer("À FAIRE", "#6B7280");
        todoColumn = createColumn();
        addColumnToContainer(todoColumnContainer, todoColumn);

        VBox doingColumnContainer = createColumnContainer("EN COURS", "#3B82F6");
        doingColumn = createColumn();
        addColumnToContainer(doingColumnContainer, doingColumn);

        VBox doneColumnContainer = createColumnContainer("TERMINÉ", "#10B981");
        doneColumn = createColumn();
        addColumnToContainer(doneColumnContainer, doneColumn);

        columnsContainer.getChildren().addAll(todoColumnContainer, doingColumnContainer, doneColumnContainer);

        return columnsContainer;
    }

    private VBox createColumnContainer(String title, String accentColor) {
        VBox columnContainer = new VBox(15);
        HBox.setHgrow(columnContainer, javafx.scene.layout.Priority.ALWAYS);
        columnContainer.setMaxWidth(Double.MAX_VALUE);
        columnContainer.setPrefWidth(320);
        columnContainer.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 12;");
        columnContainer.setPadding(new Insets(20));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4, Color.web(accentColor));
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #4B5563; -fx-letter-spacing: 0.5px;");

        header.getChildren().addAll(dot, titleLabel);
        columnContainer.getChildren().add(header);

        return columnContainer;
    }

    private VBox createColumn() {
        VBox column = new VBox(12);
        column.setStyle("-fx-background-color: transparent;");
        column.setMinHeight(500);
        column.setFillWidth(true);
        return column;
    }

    private void addColumnToContainer(VBox container, VBox column) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setFocusTraversable(false);
        scrollPane.setContent(column);
        container.getChildren().add(scrollPane);
    }

    private void handleAddTask() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter une tâche");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent root = loader.load();

            TaskDialogController dialogController = loader.getController();

            // Passer le sprint actuel à la boîte de dialogue
            dialogController.setSprint(sprint);
            dialogController.setCurrentUser(this.user);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Recharger les tâches après ajout
            // Recharger les tâches après ajout
            reloadTasks();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture de la boîte de dialogue d'ajout de tâche");
        }
    }

    private void handleEditTask(Task task) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier une tâche");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent root = loader.load();

            TaskDialogController dialogController = loader.getController();

            // Recharger la tâche avec ses collections initialisées pour éviter
            // LazyInitializationException
            Task fullTask = taskDAO.getByIdWithCollections(task.getId());
            if (fullTask != null) {
                dialogController.setTask(fullTask);
            } else {
                dialogController.setTask(task); // Fallback
            }

            dialogController.setSprint(sprint); // Passer le sprint actuel
            dialogController.setCurrentUser(this.user);

            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Recharger les tâches après édition
            // Recharger les tâches après édition
            reloadTasks();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture de la boîte de dialogue de modification de tâche");
        }
    }

    private void refreshColumns() {
        // Vérifier que les colonnes existent
        if (todoColumn == null || doingColumn == null || doneColumn == null) {
            System.err.println("Les colonnes ne sont pas initialisées");
            return;
        }

        // Vider les colonnes
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        // Vérifier que tasks n'est pas null
        if (tasks == null) {
            System.err.println("La liste des tâches est null");
            tasks = new ArrayList<>();
        }

        System.out.println("Rafraîchissement des colonnes avec " + tasks.size() + " tâches");

        // Ajouter les tâches aux colonnes appropriées
        for (Task task : tasks) {
            try {
                VBox taskCard = createTaskCard(task);
                TaskStatus status = task.getStatus();

                if (status == null) {
                    status = TaskStatus.TO_DO; // Valeur par défaut
                }

                switch (status) {
                    case TO_DO -> todoColumn.getChildren().add(taskCard);
                    case DOING -> doingColumn.getChildren().add(taskCard);
                    case DONE -> doneColumn.getChildren().add(taskCard);
                    default -> todoColumn.getChildren().add(taskCard);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la création de la carte pour la tâche: " + task.getTitle());
                e.printStackTrace();
            }
        }

        // Afficher un message si aucune tâche
        if (tasks.isEmpty()) {
            Label emptyLabel = new Label("Aucune tâche pour ce sprint");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            todoColumn.getChildren().add(emptyLabel);
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-cursor: hand;");
        card.setPadding(new Insets(10));

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(task.getDescription() != null ? task.getDescription() : "");
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setOnAction(e -> {
            try {
                taskDAO.delete(task.getId());
                taskDAO.delete(task.getId());
                reloadTasks(); // Recharger après suppression
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Erreur lors de la suppression de la tâche");
            }
        });
        deleteButton.setOnMouseClicked(e -> e.consume());

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
        editButton.setOnAction(e -> handleEditTask(task));
        editButton.setOnMouseClicked(e -> e.consume());

        buttonBox.getChildren().addAll(editButton, deleteButton);
        card.getChildren().addAll(titleLabel, descLabel, buttonBox);

        // Make card draggable
        setupDragSource(card, task);

        card.setOnMouseClicked(e -> {
            System.out.println("Click detected on task card. Count: " + e.getClickCount());
            if (e.getClickCount() == 1) { // Simple clic pour ouvrir les détails
                System.out.println("Single click detected. Opening details for task: " + task.getId());
                openTaskDetails(task);
            }
        });

        return card;
    }

    private void setupDragSource(VBox card, Task task) {
        card.setOnDragDetected(event -> {
            Dragboard dragboard = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId())); // Utiliser l'ID comme identifiant
            dragboard.setContent(content);
            card.setOpacity(0.5);
            event.consume();
        });

        card.setOnDragDone(event -> {
            card.setOpacity(1.0);
            event.consume();
        });
    }

    private void setupDropTarget(VBox column, TaskStatus targetStatus) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle("-fx-background-color: #d5dbdb; -fx-background-radius: 5;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle("-fx-background-color: transparent;");
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                try {
                    long taskId = Long.parseLong(dragboard.getString());

                    Task managedTask = taskDAO.getByIdWithCollections(taskId);
                    if (managedTask != null) {
                        managedTask.setStatus(targetStatus);
                        managedTask.addLog("Statut changé vers " + targetStatus, this.user);

                        taskDAO.update(managedTask);

                        success = true;
                    } else {
                        for (Task task : tasks) {
                            if (task.getId() == taskId) {
                                try {
                                    task.setStatus(targetStatus);
                                    taskDAO.update(task);
                                    success = true;
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            }
                        }
                    }

                    if (success) {
                        reloadTasks();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de format de l'ID de tâche: " + dragboard.getString());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Erreur lors de la mise à jour du statut de la tâche");
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void openTaskDetails(Task task) {
        System.out.println("Attempting to open details for task: " + task.getId());
        try {
            // Récupérer la tâche complète avec ses collections
            Task fullTask = taskDAO.getByIdWithCollections(task.getId());
            if (fullTask == null) {
                System.err.println("Task not found with ID: " + task.getId());
                return;
            }
            System.out.println("Task loaded: " + fullTask.getTitle());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDetailsView.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            TaskDetailsController controller = loader.getController();
            controller.setTask(fullTask);
            controller.setCurrentUser(this.user);
            System.out.println("Controller initialized");

            Stage stage = new Stage();
            stage.setTitle("Détails de la tâche - " + fullTask.getTitle());
            stage.setScene(new Scene(root, 900, 800));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
            System.out.println("Stage shown");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Erreur lors de l'ouverture des détails de la tâche: " + ex.getMessage());
        }
    }

    // Méthode pour charger les tâches par projet (optionnel)
    public void loadTasksByProject(Long projectId) {
        if (projectId != null) {
            tasks = taskDAO.getByProject(project);
        } else {
            tasks = new ArrayList<>();
        }

        if (todoColumn != null && doingColumn != null && doneColumn != null) {
            refreshColumns();
        }
    }

    // Méthode pour réinitialiser le contrôleur
    public void reset() {
        this.sprint = null;
        this.project = null;
        this.tasks = new ArrayList<>();
        if (projectCombo != null)
            projectCombo.getSelectionModel().clearSelection();
        if (sprintCombo != null) {
            sprintCombo.getItems().clear();
            sprintCombo.setDisable(true);
        }
        if (addTaskButton != null)
            addTaskButton.setVisible(false);
        refreshColumns();
    }

    private void loadProjects() {
        List<Project> projects = projectDAO.getAllProjects(); // Ou filtrer par utilisateur si nécessaire
        projectCombo.setItems(FXCollections.observableArrayList(projects));
    }

    private void handleProjectSelection() {
        Project selectedProject = projectCombo.getValue();
        if (selectedProject != null) {
            this.project = selectedProject;
            this.sprint = null; // Reset sprint selection

            // Activer le bouton d'ajout
            if (addTaskButton != null)
                addTaskButton.setVisible(true);

            // Charger les sprints du projet
            List<Sprint> sprints = sprintDAO.getAllSprintsByProject(selectedProject);
            sprintCombo.setItems(FXCollections.observableArrayList(sprints));

            sprintCombo.setDisable(false);
            reloadTasks();
        } else {
            this.project = null;
            this.sprint = null;
            sprintCombo.getItems().clear();
            sprintCombo.setDisable(true);
            if (addTaskButton != null)
                addTaskButton.setVisible(false);
            tasks.clear();
            refreshColumns();
        }
    }

    private void handleSprintSelection() {
        Sprint selectedSprint = sprintCombo.getValue();
        if (selectedSprint != null) {
            this.sprint = selectedSprint;
            this.sprint = selectedSprint;
            reloadTasks();
        } else {
            this.sprint = null;
            if (this.project != null) {
                reloadTasks();
            }
        }
    }
}