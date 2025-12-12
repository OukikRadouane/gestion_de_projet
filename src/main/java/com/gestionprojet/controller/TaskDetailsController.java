package com.gestionprojet.controller;

import com.gestionprojet.model.Tasks.*;
import com.gestionprojet.dao.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class TaskDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label priorityLabel;
    @FXML private Label assigneeLabel;
    @FXML private Label sprintLabel;

    @FXML private TextArea descriptionArea;
    @FXML private VBox subtasksContainer;
    @FXML private VBox commentsContainer;
    @FXML private TextField newCommentField;
    @FXML private TextArea logsArea;
    @FXML private TextField newSubtaskField;

    private Subtask selectedSubtask = null;
    private Comment selectedComment = null;
    private Task task;
    private final TaskDAO taskDAO = new TaskDAO();

    public void setTask(Task task) {
        this.task = task;
        loadTaskDetails();
    }

    private void loadTaskDetails() {
        titleLabel.setText(task.getTitle());
        statusLabel.setText(task.getStatus().getDisplayName());
        priorityLabel.setText(task.getPriority().name());
        assigneeLabel.setText(task.getAssignee() != null ? task.getAssignee().getUsername() : "Non assigné");
        sprintLabel.setText(task.getSprint() != null ? task.getSprint().getName() : "Aucun");

        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");

        loadSubtasks();
        loadComments();
        refreshLogs();
    }

    private void loadSubtasks() {
        subtasksContainer.getChildren().clear();

        for (Subtask subtask : task.getSubtasks()) {
            HBox subtaskBox = createSubtaskBox(subtask);
            subtasksContainer.getChildren().add(subtaskBox);
        }
    }

    private HBox createSubtaskBox(Subtask subtask) {
        HBox subtaskBox = new HBox(10);
        subtaskBox.setAlignment(Pos.CENTER_LEFT);
        subtaskBox.setStyle("-fx-padding: 5;");

        CheckBox checkBox = new CheckBox(subtask.getTitle());
        checkBox.setSelected(subtask.isDone());
        HBox.setHgrow(checkBox, Priority.ALWAYS);

        updateSubtaskStyle(checkBox, subtask.isDone());

        checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            subtask.setDone(newValue);
            updateSubtaskStyle(checkBox, newValue);

            task.addLog(newValue ?
                    "Sous-tâche terminée: " + subtask.getTitle() :
                    "Sous-tâche réouverte: " + subtask.getTitle()
            );

            saveTask();
            refreshLogs();
        });

        subtaskBox.setOnMouseClicked(e -> {
            clearSubtaskSelection();
            subtaskBox.setStyle("-fx-padding: 5; -fx-background-color: #d6eaff; -fx-background-radius: 5;");
            selectedSubtask = subtask;
        });

        subtaskBox.getChildren().addAll(checkBox);
        return subtaskBox;
    }

    private void clearSubtaskSelection() {
        for (var node : subtasksContainer.getChildren()) {
            if (node instanceof HBox) {
                node.setStyle("-fx-padding: 5;");
            }
        }
        selectedSubtask = null;
    }

    private void updateSubtaskStyle(CheckBox checkBox, boolean done) {
        checkBox.setStyle(done
                ? "-fx-text-fill: #7f8c8d; -fx-font-style: italic;"
                : "-fx-text-fill: #2c3e50;");
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();

        for (Comment comment : task.getComments()) {
            HBox commentBox = createCommentBox(comment);
            commentsContainer.getChildren().add(commentBox);
        }
    }

    private HBox createCommentBox(Comment comment) {
        HBox commentBox = new HBox(10);
        commentBox.setAlignment(Pos.CENTER_LEFT);
        commentBox.setStyle("-fx-padding: 5;");

        Label label = new Label(comment.getText());
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        // Gestion de la sélection
        commentBox.setOnMouseClicked(e -> {
            clearCommentSelection();
            commentBox.setStyle("-fx-padding: 5; -fx-background-color: #d6eaff; -fx-background-radius: 5;");
            selectedComment = comment;
        });
        commentBox.getChildren().addAll(label);
        return commentBox;
    }

    private void clearCommentSelection() {
        for (var node : commentsContainer.getChildren()) {
            if (node instanceof HBox) {
                node.setStyle("-fx-padding: 5;");
            }
        }
        selectedComment = null;
    }

    @FXML
    private void handleAddSubtask() {
        String title = newSubtaskField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un titre pour la sous-tâche");
            return;
        }

        try {
            Subtask subtask = new Subtask();
            subtask.setTitle(title);
            subtask.setDone(false);
            subtask.setTask(task);

            task.addSubtask(subtask);

            saveTask();
            loadSubtasks();

            task.addLog("Sous-tâche ajoutée: " + title);
            refreshLogs();

            newSubtaskField.clear();
            clearSubtaskSelection();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la sous-tâche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelSubtask() {
        if (selectedSubtask == null) {
            showAlert("Erreur", "Veuillez sélectionner une sous-tâche à supprimer");
            return;
        }

        if (!confirm("Supprimer la sous-tâche", "Êtes-vous sûr de vouloir supprimer la sous-tâche : " + selectedSubtask.getTitle() + " ?")) {
            return;
        }

        try {
            String subtaskTitle = selectedSubtask.getTitle();

            // Supprimer de la base de données d'abord
            taskDAO.deleteSubtask(selectedSubtask);
            // Puis supprimer de la liste en mémoire
            task.removeSubtask(selectedSubtask);

            loadSubtasks();
            task.addLog("Sous-tâche supprimée: " + subtaskTitle);
            refreshLogs();
            clearSubtaskSelection();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression de la sous-tâche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddComment() {
        String content = newCommentField.getText().trim();
        if (content.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un commentaire");
            return;
        }

        try {
            Comment comment = new Comment();
            comment.setText(content);
            comment.setTask(task); // Important: lier le commentaire à la tâche

            task.addComment(comment);

            saveTask();
            loadComments();

            task.addLog("Commentaire ajouté");
            refreshLogs();

            newCommentField.clear();
            clearCommentSelection();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelComment() {
        if (selectedComment == null) {
            showAlert("Erreur", "Veuillez sélectionner un commentaire à supprimer");
            return;
        }

        if (!confirm("Supprimer le commentaire", "Êtes-vous sûr de vouloir supprimer ce commentaire ?")) {
            return;
        }

        try {
            // Supprimer de la base de données d'abord
            taskDAO.deleteComment(selectedComment);
            // Puis supprimer de la liste en mémoire
            task.removeComment(selectedComment);

            loadComments();
            task.addLog("Commentaire supprimé");
            refreshLogs();
            clearCommentSelection();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean confirm(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(header);
        alert.setContentText(message);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshLogs() {
        StringBuilder sb = new StringBuilder();
        for (TaskLog log : task.getLogs()) {
            sb.append(log.getMessage()).append("\n");
        }
        logsArea.setText(sb.toString());
    }

    private void saveTask() {
        try {
            taskDAO.save(task);
        } catch (Exception e) {
            showAlert("Erreur de sauvegarde", "Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        try {
            saveTask();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la fermeture: " + e.getMessage());
            e.printStackTrace();
        }
    }


}