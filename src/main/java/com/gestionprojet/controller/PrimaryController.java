package com.gestionprojet.controller;

import com.gestionprojet.model.Task;
import com.gestionprojet.model.TaskStatus;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrimaryController {

    private VBox todoColumn;
    private VBox doingColumn;
    private VBox doneColumn;
    private Button addTaskButton;
    private BorderPane root;

    private List<Task> tasks = new ArrayList<>();

    public BorderPane createView() {
        root = new BorderPane();
        root.setPrefHeight(600);
        root.setPrefWidth(900);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Create top bar
        root.setTop(createTopBar());

        // Create kanban columns
        root.setCenter(createKanbanColumns());

        // Initialize data and setup
        addSampleTasks();
        refreshColumns();

        // Set up drag and drop for columns
        setupDropTarget(todoColumn, TaskStatus.TO_DO);
        setupDropTarget(doingColumn, TaskStatus.DOING);
        setupDropTarget(doneColumn,TaskStatus.DONE);

        return root;
    }
    private VBox createTopBar() {
        VBox topBar = new VBox();
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setPadding(new Insets(15, 20, 15, 20));

        Label titleLabel = new Label("Kanban Board");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System Bold", 24));

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

    private void addSampleTasks() {
        tasks.add(new Task("Setup Project", "Initialize the JavaFX project with Maven",TaskStatus.DONE));
        tasks.add(new Task("Create Kanban Board", "Design and implement the Kanban board UI", TaskStatus.DOING));
        tasks.add(new Task("Integrate Database", "Connect the application to the database", TaskStatus.TO_DO));
        tasks.add(new Task("Add User Authentication", "Implement login and registration features", TaskStatus.TO_DO));
    }

    private void handleAddTask() {
        try {
            // Crée le stage pour le dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter une tâche");

            // Charge le FXML - vérifie le chemin exact
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/TaskDialog.fxml"));
            Parent root = loader.load();

            // Récupère le contrôleur FXML
            TaskDialogController dialogController = loader.getController();

            // Configure pour une nouvelle tâche
            dialogController.setTask(null); // Pour indiquer que c'est une nouvelle tâche
            // dialogController.setSprint(null); // Décommente si nécessaire

            // Ouvre le dialogue
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // Rafraîchit les colonnes après fermeture du dialogue
            refreshColumns();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    private void refreshColumns() {
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        for (Task task : tasks) {
            VBox taskCard = createTaskCard(task);
            switch (task.getStatus()) {
                case TO_DO -> todoColumn.getChildren().add(taskCard);
                case DOING -> doingColumn.getChildren().add(taskCard);
                case DONE -> doneColumn.getChildren().add(taskCard);
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-cursor: hand;");
        card.setPadding(new Insets(10));

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(task.getDescription());
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        // Action buttons
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setOnAction(e -> {
            tasks.remove(task);
            refreshColumns();
        });
        buttonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(titleLabel, descLabel, buttonBox);

        // Make card draggable
        setupDragSource(card, task);

        return card;
    }

    private void setupDragSource(VBox card, Task task) {
        card.setOnDragDetected(event -> {
            Dragboard dragboard = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getTitle()); // Use title as identifier
            dragboard.setContent(content);
            card.setOpacity(0.5);
            event.consume();
        });

        card.setOnDragDone(event -> {
            card.setOpacity(1.0);
            event.consume();
        });
    }

    private void setupDropTarget(VBox column,TaskStatus targetStatus) {
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
                String taskTitle = dragboard.getString();
                // Find the task by title and update its status
                for (Task task : tasks) {
                    if (task.getTitle().equals(taskTitle)) {
                        task.setStatus(targetStatus);
                        success = true;
                        break;
                    }
                }
                if (success) {
                    refreshColumns();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
