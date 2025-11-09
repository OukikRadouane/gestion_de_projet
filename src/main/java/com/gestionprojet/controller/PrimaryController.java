package com.gestionprojet.controller;

import com.gestionprojet.view.KanbanTask;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrimaryController {

    private VBox todoColumn;
    private VBox doingColumn;
    private VBox doneColumn;
    private Button addTaskButton;
    private BorderPane root;

    private List<KanbanTask> tasks = new ArrayList<>();

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
        setupDropTarget(todoColumn, KanbanTask.TaskStatus.TODO);
        setupDropTarget(doingColumn, KanbanTask.TaskStatus.DOING);
        setupDropTarget(doneColumn, KanbanTask.TaskStatus.DONE);

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
        HBox.setHgrow(columnContainer, Priority.ALWAYS);
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
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scrollPane.setFocusTraversable(false);
        scrollPane.setContent(column);

        container.getChildren().add(scrollPane);
    }

    private void addSampleTasks() {
        tasks.add(new KanbanTask("Setup Project", "Initialize the JavaFX project with Maven", KanbanTask.TaskStatus.DONE));
        tasks.add(new KanbanTask("Create Kanban Board", "Design and implement the Kanban board UI", KanbanTask.TaskStatus.DOING));
        tasks.add(new KanbanTask("Integrate Database", "Connect the application to the database", KanbanTask.TaskStatus.TODO));
        tasks.add(new KanbanTask("Add User Authentication", "Implement login and registration features", KanbanTask.TaskStatus.TODO));
    }

    private void handleAddTask() {
        Dialog<KanbanTask> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Create a new task");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task Description");
        descriptionArea.setPrefRowCount(3);

        ComboBox<KanbanTask.TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(KanbanTask.TaskStatus.values());
        statusCombo.setValue(KanbanTask.TaskStatus.TODO);

        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionArea,
            new Label("Status:"), statusCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new KanbanTask(titleField.getText(), descriptionArea.getText(), statusCombo.getValue());
            }
            return null;
        });

        Optional<KanbanTask> result = dialog.showAndWait();
        result.ifPresent(task -> {
            tasks.add(task);
            refreshColumns();
        });
    }

    private void refreshColumns() {
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        for (KanbanTask task : tasks) {
            VBox taskCard = createTaskCard(task);
            switch (task.getStatus()) {
                case TODO -> todoColumn.getChildren().add(taskCard);
                case DOING -> doingColumn.getChildren().add(taskCard);
                case DONE -> doneColumn.getChildren().add(taskCard);
            }
        }
    }

    private VBox createTaskCard(KanbanTask task) {
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

    private void setupDragSource(VBox card, KanbanTask task) {
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

    private void setupDropTarget(VBox column, KanbanTask.TaskStatus targetStatus) {
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
                for (KanbanTask task : tasks) {
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
