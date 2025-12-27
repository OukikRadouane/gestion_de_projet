package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserRole;

    @FXML
    private ComboBox<com.gestionprojet.model.Project> comboActiveProject;
    @FXML
    private ComboBox<com.gestionprojet.model.Sprint> comboActiveSprint;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnProjects;
    @FXML
    private Button btnSprints;
    @FXML
    private Button btnBacklog;
    @FXML
    private Button btnKanban;
    @FXML
    private Button btnSettings;

    private AuthService authService;
    private com.gestionprojet.dao.SprintDAO sprintDAO = new com.gestionprojet.dao.SprintDAO();
    private List<Button> menuButtons = new ArrayList<>();

    private String currentViewFxml;
    // Cache pour réutiliser la vue et le contrôleur Kanban
    private kanbanController currentKanbanController;
    private Parent kanbanView;

    @FXML
    public void initialize() {
        menuButtons.add(btnDashboard);
        menuButtons.add(btnProjects);
        menuButtons.add(btnSprints);
        menuButtons.add(btnBacklog);
        menuButtons.add(btnKanban);
        menuButtons.add(btnSettings);

        setupContextSelectors();
    }

    private void setupContextSelectors() {
        comboActiveProject.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.gestionprojet.model.Project p) {
                return p != null ? p.getName() : "Aucun projet";
            }

            @Override
            public com.gestionprojet.model.Project fromString(String s) {
                return null;
            }
        });

        comboActiveSprint.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.gestionprojet.model.Sprint s) {
                return s != null ? s.getName() : "Aucun sprint";
            }

            @Override
            public com.gestionprojet.model.Sprint fromString(String s) {
                return null;
            }
        });

        comboActiveProject.setOnAction(e -> {
            com.gestionprojet.model.Project selected = comboActiveProject.getValue();
            System.out.println(
                    "MainController: Projet sélectionné -> " + (selected != null ? selected.getName() : "null"));
            loadSprintsForProject(selected);
            refreshCurrentView();
        });

        comboActiveSprint.setOnAction(e -> {
            com.gestionprojet.model.Sprint selected = comboActiveSprint.getValue();
            System.out.println(
                    "MainController: Sprint sélectionné -> " + (selected != null ? selected.getName() : "null"));
            refreshCurrentView();
        });
    }

    private void loadProjects() {
        if (authService != null && authService.isLoggedIn()) {
            List<com.gestionprojet.model.Project> projects = authService.getAllProjectsOfCurrentUser();
            comboActiveProject.getItems().setAll(projects);
            if (!projects.isEmpty() && comboActiveProject.getValue() == null) {
                comboActiveProject.setValue(projects.get(0));
            }
        }
    }

    public void selectProject(com.gestionprojet.model.Project project) {
        if (project != null) {
            comboActiveProject.setValue(project);
            loadSprintsForProject(project);
        }
    }

    public void selectSprint(com.gestionprojet.model.Sprint sprint) {
        if (sprint != null) {
            // Ensure project is selected first
            if (sprint.getProject() != null) {
                selectProject(sprint.getProject());
            }
            comboActiveSprint.setValue(sprint);
        }
    }

    private void loadSprintsForProject(com.gestionprojet.model.Project project) {
        if (project != null) {
            List<com.gestionprojet.model.Sprint> sprints = new ArrayList<>();
            sprints.add(null); // Option "Aucun sprint"
            sprints.addAll(sprintDAO.getAllSprintsByProject(project));

            comboActiveSprint.getItems().setAll(sprints);

            // On ne force pas la sélection du premier sprint si "Aucun" est souhaité
            // ou si on veut garder la sélection actuelle (si toujours valide)
            if (comboActiveSprint.getValue() == null || !sprints.contains(comboActiveSprint.getValue())) {
                comboActiveSprint.setValue(null);
            }
            comboActiveSprint.setDisable(false);
        } else {
            comboActiveSprint.getItems().clear();
            comboActiveSprint.setDisable(true);
        }
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        if (authService != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            lblUserName.setText(user.getUsername());
            lblUserRole.setText(user.getRole().toString());
            refreshProjects();
        }
        showDashboard();
    }

    public void refreshProjects() {
        loadProjects();
    }

    public void refreshSprints() {
        loadSprintsForProject(comboActiveProject.getValue());
    }

    private void setActiveButton(Button activeButton) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("sidebar-item-active");
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-item-active");
        }
    }

    private void refreshCurrentView() {
        if (currentViewFxml != null) {
            // Re-load the current view with updated context
            if (currentViewFxml.equals("KANBAN")) {
                showKanban();
            } else {
                loadView(currentViewFxml, null);
            }
        }
    }

    private void loadView(String fxmlPath, Button triggerButton) {
        currentViewFxml = fxmlPath;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                DashboardController dc = (DashboardController) controller;
                dc.setAuthService(authService);
                dc.setMainController(this);
            } else if (controller instanceof SprintsViewController) {
                SprintsViewController svc = (SprintsViewController) controller;
                svc.setMainController(this);
                svc.setProject(comboActiveProject.getValue());
                if (authService != null) {
                    svc.setCurrentUser(authService.getCurrentUser());
                }
            } else if (controller instanceof BacklogController) {
                BacklogController bc = (BacklogController) controller;
                bc.setProject(comboActiveProject.getValue());
                if (authService != null)
                    bc.setCurrentUser(authService.getCurrentUser());
            } else if (controller instanceof ProfileController) {
                ProfileController pc = (ProfileController) controller;
                pc.setAuthService(authService);
                pc.setMainController(this);
            }

            contentArea.getChildren().setAll(view);
            if (triggerButton != null)
                setActiveButton(triggerButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/view/dashboard.fxml", btnDashboard);
    }

    @FXML
    public void showProjects() {
        loadView("/view/projects-list.fxml", btnProjects);
    }

    @FXML
    public void showSprints() {
        loadView("/view/sprints-view.fxml", btnSprints);
    }

    @FXML
    public void showBacklog() {
        loadView("/view/backlog.fxml", btnBacklog);
    }

    @FXML
    public void showKanban() {
        System.out.println("MainController: Switching to Kanban View...");
        currentViewFxml = "KANBAN";

        com.gestionprojet.model.Project p = comboActiveProject.getValue();
        com.gestionprojet.model.Sprint s = comboActiveSprint.getValue();
        User currentUser = authService != null ? authService.getCurrentUser() : null;

        if (p == null) {
            System.out.println("Kanban: Aucun projet sélectionné. Annulation.");
            Label placeholder = new Label("Veuillez sélectionner un projet dans la barre latérale");
            placeholder.getStyleClass().add("h2");
            contentArea.getChildren().setAll(new StackPane(placeholder));
            return;
        }

        System.out.println("Kanban Loading - Project: " + p.getName() +
                ", Sprint: " + (s != null ? s.getName() : "NONE") +
                ", User: " + (currentUser != null ? currentUser.getUsername() : "NONE"));

        try {
            if (currentKanbanController == null) {
                currentKanbanController = new kanbanController();
                if (currentUser != null) {
                    currentKanbanController.setUser(currentUser);
                }
                currentKanbanController.setInitialContext(p, s);
                kanbanView = currentKanbanController.createView();
                if (kanbanView != null) {
                    contentArea.getChildren().setAll(kanbanView);
                    setActiveButton(btnKanban);
                    System.out.println("Kanban: Vue injectée avec succès.");
                } else {
                    System.err.println("Kanban: createView() a retourné null !");
                }
            } else {
                // Réutiliser le contrôleur existant: mettre à jour l'utilisateur et le contexte
                if (currentUser != null) {
                    currentKanbanController.setUser(currentUser);
                }
                currentKanbanController.setInitialContext(p, s);

                if (kanbanView == null) {
                    kanbanView = currentKanbanController.createView();
                }
                if (kanbanView != null) {
                    contentArea.getChildren().setAll(kanbanView);
                    setActiveButton(btnKanban);
                }
                System.out.println("Kanban: Vue mise à jour avec le contexte: Project="
                        + (p != null ? p.getName() : "None") + ", Sprint=" + (s != null ? s.getName() : "None"));
            }
        } catch (Exception e) {
            System.err.println("Critical error in showKanban:");
            e.printStackTrace();
        }
    }

    @FXML
    private void showSettings() {
        loadView("/view/profile.fxml", btnSettings);
    }

    @FXML
    public void handleLogout() {
        if (authService != null) {
            authService.logout();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            AuthController controller = loader.getController();
            controller.setAuthService(authService);
            lblUserName.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
