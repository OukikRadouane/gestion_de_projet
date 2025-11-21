package com.gestionprojet.controller;

import com.gestionprojet.model.*;
import dao.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TaskDialogController {
    @FXML private Text titleText;
    @FXML private TextField taskNameField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<TaskStatus> statusChoice;
    @FXML private ComboBox<Priority>  priorityChoice;
    @FXML private ComboBox<User> assigneChoice;

    @FXML private Label errorLabel;

    private  final TaskDAO taskDAO = new TaskDAO();
    //private final UserDAO userDAO = new UserDAO();
    private Sprint sprint;
    private Task task;

    @FXML
    public void initialize() {
        priorityChoice.getItems().addAll(Priority.values());
        priorityChoice.setValue(Priority.MEDIUM);

        statusChoice.getItems().addAll(TaskStatus.values());
        statusChoice.setValue(TaskStatus.TO_DO);

        priorityChoice.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                setText(empty || priority == null ? null : priority.getDisplayName());
            }
        });
        priorityChoice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                setText(empty || priority == null ? null : priority.getDisplayName());
            }
        });

        statusChoice.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TaskStatus status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty || status == null ? null : status.getDisplayName());
            }
        });
        statusChoice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TaskStatus status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty || status == null ? null : status.getDisplayName());
            }
        });



        dueDatePicker.setValue(LocalDate.now().plusDays(7));
    }



    public void saveTask(){
        String name = taskNameField.getText();
        String description = descriptionField.getText();
        LocalDate deadline = dueDatePicker.getValue();
        TaskStatus  status = statusChoice.getValue();
        Priority priority = priorityChoice.getValue();
        User assignee = assigneChoice.getValue();

        if (name.isEmpty()){
            showError("Le nom de la tâche est requis");
            return;
        }
        if (status == null){
            showError("Le status est requis");
            return;
        }
        if (priority == null){
            showError("La priority est requis");
            return;
        }
        try {
            if( task == null){
                task = new Task(name, description,  status, priority,deadline, assignee, sprint);
                taskDAO.save(task);
            }
            else {
                task.setTitle(name);
                task.setDescription(description);
                task.setStatus(status);
                task.setPriority(priority);
                task.setDeadline(deadline);
                task.setAssignee(assignee);
                task.setSprint(sprint);
                taskDAO.update(task);
            }
            System.out.println("✅ tache insérés avec succès !");
            closeDialog();
    } catch (Exception e) {
        e.printStackTrace();
        showError("Erreur lors de l'enregistrement de la tâche");
    }
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null) {
            titleText.setText("Modifier Tâche");
            taskNameField.setText(task.getTitle());
            descriptionField.setText(task.getDescription());
            priorityChoice.setValue(task.getPriority());
            statusChoice.setValue(task.getStatus());
            dueDatePicker.setValue(task.getDeadline());
            assigneChoice.setValue(task.getAssignee());
        }
    }

    public void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        closeDialog();
        System.out.println("✅ action annulee !");
    }

    private void closeDialog() {
        Stage stage = (Stage) taskNameField.getScene().getWindow();
        stage.close();
    }
    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }
    public Task getTask() {
        return task;
    }



}
