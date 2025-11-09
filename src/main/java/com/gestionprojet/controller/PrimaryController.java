package com.gestionprojet.controller;

import com.gestionprojet.view.KanbanTask;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {

    @FXML
    private VBox todoColumn;

    @FXML
    private VBox doingColumn;

    @FXML
    private VBox doneColumn;

    @FXML
    private Button addTaskButton;

    private List<KanbanTask> tasks = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add some sample tasks
        addSampleTasks();
        refreshColumns();
    }

    private void addSampleTasks() {
        tasks.add(new KanbanTask("Setup Project", "Initialize the JavaFX project with Maven", KanbanTask.TaskStatus.DONE));
        tasks.add(new KanbanTask("Create Kanban Board", "Design and implement the Kanban board UI", KanbanTask.TaskStatus.DOING));
        tasks.add(new KanbanTask("Integrate Database", "Connect the application to the database", KanbanTask.TaskStatus.TODO));
        tasks.add(new KanbanTask("Add User Authentication", "Implement login and registration features", KanbanTask.TaskStatus.TODO));
    }

    @FXML
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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        card.setPadding(new Insets(10));

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(task.getDescription());
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        // Action buttons
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (task.getStatus() != KanbanTask.TaskStatus.TODO) {
            Button moveLeftButton = new Button("←");
            moveLeftButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
            moveLeftButton.setOnAction(e -> {
                moveTaskLeft(task);
            });
            buttonBox.getChildren().add(moveLeftButton);
        }

        if (task.getStatus() != KanbanTask.TaskStatus.DONE) {
            Button moveRightButton = new Button("→");
            moveRightButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 10px;");
            moveRightButton.setOnAction(e -> {
                moveTaskRight(task);
            });
            buttonBox.getChildren().add(moveRightButton);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBox.getChildren().add(spacer);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setOnAction(e -> {
            tasks.remove(task);
            refreshColumns();
        });
        buttonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(titleLabel, descLabel, buttonBox);
        return card;
    }

    private void moveTaskLeft(KanbanTask task) {
        switch (task.getStatus()) {
            case DOING -> task.setStatus(KanbanTask.TaskStatus.TODO);
            case DONE -> task.setStatus(KanbanTask.TaskStatus.DOING);
        }
        refreshColumns();
    }

    private void moveTaskRight(KanbanTask task) {
        switch (task.getStatus()) {
            case TODO -> task.setStatus(KanbanTask.TaskStatus.DOING);
            case DOING -> task.setStatus(KanbanTask.TaskStatus.DONE);
        }
        refreshColumns();
    }
}
