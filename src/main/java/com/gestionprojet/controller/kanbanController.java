package com.gestionprojet.controller;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskStatus;
import com.gestionprojet.dao.TaskDAO;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    private final TaskDAO taskDAO = new TaskDAO();

    public kanbanController() {
        this.tasks = new ArrayList<>();
    }

    private void loadTasksForSprint() {
        if (sprint != null) {
            tasks = taskDAO.getBySprint(sprint);
            System.out.println("Chargement des tâches pour le sprint: " + sprint.getName() + ", nombre de tâches: " + tasks.size());
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
        loadTasksForSprint();
    }

    public BorderPane createView() {
        root = new BorderPane();
        root.setPrefHeight(600);
        root.setPrefWidth(900);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Create top bar
        root.setTop(createTopBar());

        // Create kanban columns
        root.setCenter(createKanbanColumns());

        setupDropTarget(todoColumn, TaskStatus.TO_DO);
        setupDropTarget(doingColumn, TaskStatus.DOING);
        setupDropTarget(doneColumn, TaskStatus.DONE);

        // Ne pas appeler refreshColumns ici, attendre que le sprint soit défini
        return root;
    }

    private VBox createTopBar() {
        VBox topBar = new VBox();
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setPadding(new Insets(15, 20, 15, 20));

        Label titleLabel = new Label("Kanban Board");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System Bold", 24));

        // Afficher le nom du sprint si disponible
        if (sprint != null) {
            Label sprintLabel = new Label("Sprint: " + sprint.getName());
            sprintLabel.setTextFill(Color.LIGHTGRAY);
            sprintLabel.setFont(Font.font("System", 14));
            topBar.getChildren().add(sprintLabel);
        }

        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));

        addTaskButton = new Button("+ Add Task");
        addTaskButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addTaskButton.setOnAction(e -> handleAddTask());

        buttonContainer.getChildren().add(addTaskButton);
        topBar.getChildren().addAll(titleLabel, buttonContainer);

        return topBar;
    }

    private HBox createKanbanColumns() {
        HBox columnsContainer = new HBox(15);
        columnsContainer.setAlignment(Pos.TOP_CENTER);
        columnsContainer.setStyle("-fx-background-color: #f5f5f5;");
        columnsContainer.setPadding(new Insets(20, 20, 20, 20));

        // Create TODO column
        VBox todoColumnContainer = createColumnContainer("TO DO");
        todoColumn = createColumn();
        addColumnToContainer(todoColumnContainer, todoColumn);

        // Create DOING column
        VBox doingColumnContainer = createColumnContainer("DOING");
        doingColumn = createColumn();
        addColumnToContainer(doingColumnContainer, doingColumn);

        // Create DONE column
        VBox doneColumnContainer = createColumnContainer("DONE");
        doneColumn = createColumn();
        addColumnToContainer(doneColumnContainer, doneColumn);

        columnsContainer.getChildren().addAll(todoColumnContainer, doingColumnContainer, doneColumnContainer);

        return columnsContainer;
    }

    private VBox createColumnContainer(String title) {
        VBox columnContainer = new VBox(10);
        HBox.setHgrow(columnContainer, javafx.scene.layout.Priority.ALWAYS);
        columnContainer.setMaxWidth(Double.MAX_VALUE);
        columnContainer.setPrefWidth(300);
        columnContainer.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        columnContainer.setPadding(new Insets(10, 10, 10, 10));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        columnContainer.getChildren().add(titleLabel);

        return columnContainer;
    }

    private VBox createColumn() {
        VBox column = new VBox(10);
        column.setStyle("-fx-background-color: transparent;");
        column.setMinHeight(400);
        column.setFillWidth(true);
        column.setPadding(new Insets(5, 5, 5, 5));

        return column;
    }

    private void addColumnToContainer(VBox container, VBox column) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        scrollPane.setStyle("-fx-background: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
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
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Recharger les tâches après ajout
            loadTasksForSprint();

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
            dialogController.setTask(task);
            dialogController.setSprint(sprint); // Passer le sprint actuel

            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Recharger les tâches après édition
            loadTasksForSprint();

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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-cursor: hand;");
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
                loadTasksForSprint(); // Recharger après suppression
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Erreur lors de la suppression de la tâche");
            }
        });

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
        editButton.setOnAction(e -> handleEditTask(task));

        buttonBox.getChildren().addAll(editButton, deleteButton);
        card.getChildren().addAll(titleLabel, descLabel, buttonBox);

        // Make card draggable
        setupDragSource(card, task);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { // Double-clic pour ouvrir les détails
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

                    // Trouver la tâche par ID
                    for (Task task : tasks) {
                        if (task.getId() == taskId) {
                            // Mettre à jour le statut
                            task.setStatus(targetStatus);

                            // Sauvegarder en base de données
                            taskDAO.update(task);

                            success = true;
                            break;
                        }
                    }

                    if (success) {
                        // Recharger les tâches pour refléter le changement
                        loadTasksForSprint();
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
        try {
            // Récupérer la tâche complète avec ses collections
            Task fullTask = taskDAO.getByIdWithCollections(task.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDetailsView.fxml"));
            Parent root = loader.load();

            TaskDetailsController controller = loader.getController();
            controller.setTask(fullTask);

            Stage stage = new Stage();
            stage.setTitle("Détails de la tâche - " + fullTask.getTitle());
            stage.setScene(new Scene(root, 900, 800));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Erreur lors de l'ouverture des détails de la tâche");
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
        this.tasks = new ArrayList<>();
        refreshColumns();
    }
}