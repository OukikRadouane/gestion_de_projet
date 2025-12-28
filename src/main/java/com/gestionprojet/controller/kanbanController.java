package com.gestionprojet.controller;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskStatus;
import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.dao.TaskDAO;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

public class kanbanController {

    private VBox backlogColumn;
    private VBox todoColumn;
    private VBox inProgressColumn;
    private VBox doneColumn;
    private BorderPane root;
    private Sprint sprint;
    private User user;
    private Project project;
    private VBox backlogColumnContainer;
    private Label backlogTitleLabel;
    private List<Task> tasks;
    private Button btnStartSprint;
    private Button btnCompleteSprint;

    private final TaskDAO taskDAO = new TaskDAO();
    private final SprintDAO sprintDAO = new SprintDAO();

    public void setInitialContext(Project project, Sprint sprint) {
        this.project = project;
        this.sprint = sprint;
        reloadTasks();
    }

    public kanbanController() {
        this.tasks = new ArrayList<>();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void loadTasksForSprint() {
        if (project != null) {
            System.out.println("Kanban: Chargement des tâches pour Projet ID=" + project.getId() +
                    (sprint != null ? " et Sprint ID=" + sprint.getId() : " (Vue Globale)"));

            // On charge TOUTES les tâches du projet.
            // Le filtrage par sprint se fera lors de la distribution dans les colonnes pour
            // que
            // le backlog du projet reste toujours visible dans la colonne Backlog du
            // Kanban.
            this.tasks = taskDAO.getByProject(project);

            System.out.println("Kanban: Total tâches chargées=" + tasks.size());
        } else {
            this.tasks = new ArrayList<>();
        }

        if (todoColumn != null && inProgressColumn != null && doneColumn != null && backlogColumn != null) {
            refreshColumns();
        }
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
        System.out.println("Sprint défini dans kanbanController: " + (sprint != null ? sprint.getName() : "null"));
        loadTasksForSprint();
    }

    private void reloadTasks() {
        loadTasksForSprint();
    }

    public Parent createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        root.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Create kanban columns
        root.setTop(createTopBar());
        root.setCenter(createKanbanColumns());

        setupDropTarget(backlogColumn, TaskStatus.BACKLOG);
        setupDropTarget(todoColumn, TaskStatus.TO_DO);
        setupDropTarget(inProgressColumn, TaskStatus.DOING);
        setupDropTarget(doneColumn, TaskStatus.DONE);

        // Ensure the root fills the space
        root.setPrefWidth(Double.MAX_VALUE);
        root.setPrefHeight(Double.MAX_VALUE);

        refreshColumns();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 30, 0, 30));
        topBar.setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("Tableau Kanban");
        titleLabel.getStyleClass().add("h1");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnAddTask = new Button("Ajouter une tâche");
        btnAddTask.getStyleClass().add("button-primary");
        btnAddTask.setOnAction(e -> handleAddTask());

        btnStartSprint = new Button("Démarrer le Sprint");
        btnStartSprint.getStyleClass().add("button-primary");
        btnStartSprint.setOnAction(e -> handleStartSprint());

        btnCompleteSprint = new Button("Terminer le Sprint");
        btnCompleteSprint.getStyleClass().add("button-outline");
        btnCompleteSprint.setStyle("-fx-text-fill: #EF4444; -fx-border-color: #EF4444;");
        btnCompleteSprint.setOnAction(e -> handleCompleteSprint());

        refreshButtons();

        topBar.getChildren().addAll(titleLabel, spacer, btnAddTask, btnStartSprint, btnCompleteSprint);
        return topBar;
    }

    private void refreshButtons() {
        if (btnStartSprint != null && btnCompleteSprint != null) {
            btnStartSprint.setVisible(
                    sprint != null && sprint.getStatus() == com.gestionprojet.model.enums.SprintStatus.PLANNED);
            btnStartSprint.setManaged(btnStartSprint.isVisible());

            btnCompleteSprint.setVisible(
                    sprint != null && sprint.getStatus() == com.gestionprojet.model.enums.SprintStatus.ACTIVE);
            btnCompleteSprint.setManaged(btnCompleteSprint.isVisible());
        }
    }

    private void handleStartSprint() {
        if (sprint == null)
            return;
        try {
            sprint.setStatus(com.gestionprojet.model.enums.SprintStatus.ACTIVE);
            sprintDAO.update(sprint);
            System.out.println("🚀 Sprint démarré !");
            refreshButtons();
            reloadTasks();
            // Note: In a real app we'd refresh the top bar specifically
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCompleteSprint() {
        if (sprint == null)
            return;

        try {
            // Logique de clôture du sprint (Directe, sans service)
            List<Task> sprintTasks = taskDAO.getBySprint(sprint);

            for (Task t : sprintTasks) {
                if (t.getStatus() != TaskStatus.DONE) {
                    t.setSprint(null);
                    t.setStatus(TaskStatus.BACKLOG);
                    t.addLog("Sprint terminé - Tâche non terminée reportée au Backlog", user);
                    taskDAO.update(t);
                }
            }

            sprint.setStatus(com.gestionprojet.model.enums.SprintStatus.COMPLETED);
            sprintDAO.update(sprint);

            System.out.println("✅ Sprint terminé et tâches reportées au Backlog.");
            refreshButtons();
            reloadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Parent createKanbanColumns() {
        HBox columnsContainer = new HBox(25);
        columnsContainer.setAlignment(Pos.TOP_CENTER);
        columnsContainer.setPadding(new Insets(30));

        // Create columns with corresponding status titles
        backlogColumnContainer = createColumnContainer("BACKLOG PRODUIT", "#6B7281");
        backlogTitleLabel = (Label) ((HBox) backlogColumnContainer.getChildren().get(0)).getChildren().get(1);
        backlogColumn = createColumn();
        addColumnToContainer(backlogColumnContainer, backlogColumn);

        VBox todoColumnContainer = createColumnContainer("À FAIRE", "#6B7280");
        todoColumn = createColumn();
        addColumnToContainer(todoColumnContainer, todoColumn);

        VBox inProgressColumnContainer = createColumnContainer("EN COURS", "#3B82F6");
        inProgressColumn = createColumn();
        addColumnToContainer(inProgressColumnContainer, inProgressColumn);

        VBox doneColumnContainer = createColumnContainer("TERMINÉ", "#10B981");
        doneColumn = createColumn();
        addColumnToContainer(doneColumnContainer, doneColumn);

        columnsContainer.getChildren().addAll(backlogColumnContainer, todoColumnContainer,
                inProgressColumnContainer, doneColumnContainer);

        // Wrap in ScrollPane for horizontal scrolling if window is too small
        ScrollPane boardScroll = new ScrollPane(columnsContainer);
        boardScroll.setFitToHeight(true);
        boardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        boardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        boardScroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        return boardScroll;
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

    private void handleAddTask() {
        if (project == null) {
            System.err.println("Aucun projet sélectionné pour ajouter une tâche");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent rootNode = loader.load();

            TaskDialogController controller = loader.getController();
            controller.setProject(this.project);
            controller.setSprint(this.sprint);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(rootNode);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            setupStageInteractions(stage, rootNode);

            stage.showAndWait();
            reloadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void refreshColumns() {
        // Vérifier que les colonnes existent
        if (backlogColumn == null || todoColumn == null || inProgressColumn == null || doneColumn == null) {
            System.err.println("Les colonnes ne sont pas initialisées");
            return;
        }

        // Vider les colonnes
        backlogColumn.getChildren().clear();
        todoColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        // Vérifier que tasks n'est pas null
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        System.out.println("Kanban: Distribution de " + tasks.size() + " tâches dans les colonnes.");

        if (backlogTitleLabel != null) {
            backlogTitleLabel.setText("BACKLOG PRODUIT");
        }

        // Ajouter les tâches aux colonnes appropriées
        for (Task task : tasks) {
            try {
                VBox taskCard = createTaskCard(task);
                TaskStatus status = task.getStatus() != null ? task.getStatus() : TaskStatus.TO_DO;

                // Distribution des tâches
                if (status == TaskStatus.BACKLOG || (this.sprint != null && task.getSprint() == null)) {
                    // Les tâches au statut BACKLOG vont toujours dans la colonne Backlog.
                    // AUSSI, dans une vue sprint, les tâches qui n'ont pas de sprint (Backlog
                    // Produit)
                    // doivent apparaître dans cette colonne pour être visibles et planifiables.
                    backlogColumn.getChildren().add(taskCard);
                } else {
                    // Pour les colonnes de travail (TODO, DOING, DONE) :
                    // 1. Si aucun sprint n'est sélectionné (Vue Globale), on affiche tout ce qui
                    // n'est pas au statut BACKLOG.
                    // 2. Si un sprint est sélectionné, on n'affiche que les tâches de CE sprint.
                    if (this.sprint == null
                            || (task.getSprint() != null && task.getSprint().getId().equals(this.sprint.getId()))) {
                        switch (status) {
                            case TO_DO -> todoColumn.getChildren().add(taskCard);
                            case DOING -> inProgressColumn.getChildren().add(taskCard);
                            case DONE -> doneColumn.getChildren().add(taskCard);
                            default -> {
                            }
                        }
                    }
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
        VBox card = new VBox(12);
        card.getStyleClass().add("kanban-card");
        card.setPrefWidth(280);
        card.setCursor(javafx.scene.Cursor.HAND);

        HBox titleRow = new HBox(8);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("h2");
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 14px;");
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        titleRow.getChildren().add(titleLabel);

        // Indicateur de retard PROMINENT
        if (task.getDeadline() != null && task.getDeadline().isBefore(java.time.LocalDate.now())
                && task.getStatus() != TaskStatus.DONE) {
            Label lateLabel = new Label("⚠️ EN RETARD");
            lateLabel.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 2 6; -fx-background-radius: 4; -fx-border-color: #B91C1C; -fx-border-radius: 4; -fx-border-width: 0.5;");
            titleRow.getChildren().add(lateLabel);
            card.setStyle(card.getStyle()
                    + "; -fx-border-color: #B91C1C; -fx-border-width: 1.5; -fx-background-color: #FFF1F2;");
        }

        // Tag de sprint si en mode projet global
        if (this.sprint == null && task.getSprint() != null) {
            Label sprintTag = new Label(task.getSprint().getName());
            sprintTag.setStyle(
                    "-fx-background-color: #E0E7FF; -fx-text-fill: #4338CA; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(sprintTag);
        }

        // Badge Reporté
        boolean isReported = task.getLogs() != null && task.getLogs().stream()
                .anyMatch(log -> log.getMessage().contains("reportée au Backlog"));
        if (isReported && task.getSprint() == null) {
            Label reportedTag = new Label("REPORTÉE");
            reportedTag.setStyle(
                    "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(reportedTag);
        }

        Label descLabel = new Label(task.getDescription() != null ? task.getDescription() : "Aucune description");
        descLabel.getStyleClass().add("small-text");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Priority Badge
        Label prioLabel = new Label(task.getPriority().getDisplayName().toUpperCase());
        String prioColor = switch (task.getPriority()) {
            case HIGH -> "#EF4444";
            case LOW -> "#94A3B8";
            default -> "#3B82F6";
        };
        prioLabel.setStyle("-fx-text-fill: " + prioColor + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Assignee
        Label assigneeLabel = new Label(
                task.getAssignee() != null ? task.getAssignee().getUsername().substring(0, 1).toUpperCase() : "?");
        assigneeLabel.setStyle(
                "-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-font-size: 10px; -fx-font-weight: bold; " +
                        "-fx-min-width: 22; -fx-min-height: 22; -fx-background-radius: 11; -fx-alignment: center;");

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("button-outline");
        btnDetails.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
        btnDetails.setOnAction(e -> openTaskDetails(task));

        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().add("button-outline");
        btnEdit.setStyle("-fx-padding: 4 8; -fx-font-size: 11px; -fx-text-fill: #3B82F6;");
        btnEdit.setOnAction(e -> {
            e.consume();
            handleEditTask(task);
        });

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("button-outline");
        btnDelete.setStyle("-fx-padding: 4 8; -fx-font-size: 11px; -fx-text-fill: #EF4444;");
        btnDelete.setOnAction(e -> {
            e.consume();
            handleDeleteTask(task);
        });

        footer.getChildren().addAll(prioLabel, spacer, assigneeLabel, btnDetails, btnEdit, btnDelete);
        card.getChildren().addAll(titleRow, descLabel, footer);

        setupDragSource(card, task);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
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
                        if (targetStatus == TaskStatus.BACKLOG) {
                            managedTask.setSprint(null);
                        } else if (managedTask.getSprint() == null && this.sprint != null) {
                            // Si la tâche vient du backlog et est déplacée vers une colonne de sprint, lui
                            // assigner le sprint actuel
                            managedTask.setSprint(this.sprint);
                        }
                        managedTask.addLog("Statut changé vers " + targetStatus
                                + (managedTask.getSprint() != null
                                        ? " (Sprint: " + managedTask.getSprint().getName() + ")"
                                        : ""),
                                this.user);

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

    private void handleEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
            Parent root = loader.load();

            TaskDialogController controller = loader.getController();
            controller.setProject(this.project);
            controller.setSprint(this.sprint);
            controller.setTask(task);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Centrer et Déplacement
            setupStageInteractions(stage, root);

            stage.showAndWait();
            reloadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupStageInteractions(Stage stage, Parent rootNode) {
        // Centrer la fenêtre relative à la fenêtre principale
        stage.setOnShown(e -> {
            Stage owner = (Stage) root.getScene().getWindow();
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });

        // Permettre le déplacement
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
    }

    private void handleDeleteTask(Task task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer la tâche ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + task.getTitle() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    taskDAO.delete(task.getId());
                    reloadTasks();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root, 900, 800);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Centrer et Déplacement
            setupStageInteractions(stage, root);

            stage.show();
            System.out.println("Stage shown");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Erreur lors de l'ouverture des détails de la tâche: " + ex.getMessage());
        }
    }

    // Méthode pour charger les tâches par projet (optionnel)
    public void loadTasksByProject() {
        if (project != null) {
            tasks = taskDAO.getByProject(this.project);
        } else {
            tasks = new ArrayList<>();
        }

        if (todoColumn != null && inProgressColumn != null && doneColumn != null) {
            refreshColumns();
        }
    }

    // Méthode pour réinitialiser le contrôleur
    public void reset() {
        this.sprint = null;
        this.project = null;
        this.tasks = new ArrayList<>();
        refreshColumns();
    }

}