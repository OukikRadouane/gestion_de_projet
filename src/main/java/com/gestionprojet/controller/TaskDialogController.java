package com.gestionprojet.controller;

import com.gestionprojet.model.*;
import com.gestionprojet.model.Tasks.Priority;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskStatus;
import com.gestionprojet.dao.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TaskDialogController {
    @FXML
    private Text titleText;
    @FXML
    private TextField taskNameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<TaskStatus> statusChoice;
    @FXML
    private ComboBox<Priority> priorityChoice;

    @FXML
    private ComboBox<User> assigneChoice;
    @FXML
    private Label errorLabel;

    private final TaskDAO taskDAO = new TaskDAO();
    private final com.gestionprojet.dao.UserDAO userDAO = new com.gestionprojet.dao.UserDAO();
    private Sprint sprint;
    private Project project;
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

        // Initialiser le sélecteur d'utilisateurs
        assigneChoice.getItems().addAll(userDAO.findAll());

        // Définir comment afficher les utilisateurs dans la liste déroulante
        javafx.util.StringConverter<User> userConverter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getUsername() : "";
            }

            @Override
            public User fromString(String string) {
                return null; // Pas nécessaire pour la sélection
            }
        };

        assigneChoice.setConverter(userConverter);

        // Définir l'affichage des cellules (liste déroulante ouverte)
        assigneChoice.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getUsername());
            }
        });

        // Définir l'affichage du bouton (valeur sélectionnée)
        assigneChoice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getUsername());
            }
        });

        dueDatePicker.setValue(LocalDate.now().plusDays(7));
    }

    private com.gestionprojet.model.User currentUser;

    public void setCurrentUser(com.gestionprojet.model.User user) {
        this.currentUser = user;
    }

    public void saveTask() {
        String name = taskNameField.getText();
        String description = descriptionField.getText();
        LocalDate deadline = dueDatePicker.getValue();
        TaskStatus status = statusChoice.getValue();
        Priority priority = priorityChoice.getValue();
        User assignee = assigneChoice.getValue();

        if (name.isEmpty()) {
            showError("Le nom de la tâche est requis");
            return;
        }
        if (status == null) {
            showError("Le status est requis");
            return;
        }
        if (priority == null) {
            showError("La priority est requis");
            return;
        }
        try {
            if (task == null) {
                task = new Task(name, description, status, priority, deadline, assignee, sprint, project);
                if (sprint != null && sprint.getStatus() == com.gestionprojet.model.enums.SprintStatus.COMPLETED
                        && status != TaskStatus.DONE) {
                    System.out.println(
                            "⚠️ Création d'une tâche non terminée dans un sprint clos. Redirection vers le backlog.");
                    task.setSprint(null);
                    task.setStatus(TaskStatus.BACKLOG);
                }
                task.addLog("Tâche créée", currentUser);
                taskDAO.save(task);
                System.out.println("✅ tache inséré avec succès !");
            } else {
                task.setTitle(name);
                task.setDescription(description);
                task.setStatus(status);
                task.setPriority(priority);
                task.setDeadline(deadline);
                task.setAssignee(assignee);
                if (sprint != null && sprint.getStatus() == com.gestionprojet.model.enums.SprintStatus.COMPLETED
                        && status != TaskStatus.DONE) {
                    System.out.println(
                            "⚠️ Tentative d'assignation d'une tâche non terminée à un sprint clos. Redirection vers le backlog.");
                    task.setSprint(null);
                    task.setStatus(TaskStatus.BACKLOG);
                } else {
                    task.setSprint(sprint);
                }
                task.addLog("Tâche modifiée", currentUser);
                taskDAO.update(task);
                System.out.println("✅ tache modifié avec succès !");
            }

            closeDialog();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'enregistrement de la tâche");
        }
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null) {
            taskNameField.setText(task.getTitle());
            descriptionField.setText(task.getDescription());
            priorityChoice.setValue(task.getPriority());
            statusChoice.setValue(task.getStatus());
            dueDatePicker.setValue(task.getDeadline());
            assigneChoice.setValue(task.getAssignee());
            this.project = task.getProject();
            this.sprint = task.getSprint();
        }
    }

    public void showError(String message) {
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

    public void setProject(Project project) {
        this.project = project;
    }

    public Task getTask() {
        return task;
    }

}
