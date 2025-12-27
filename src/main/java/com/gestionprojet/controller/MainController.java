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
            loadSprintsForProject(comboActiveProject.getValue());
            refreshCurrentView();
        });

        comboActiveSprint.setOnAction(e -> refreshCurrentView());
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

    private void loadSprintsForProject(com.gestionprojet.model.Project project) {
        if (project != null) {
            List<com.gestionprojet.model.Sprint> sprints = sprintDAO.getAllSprintsByProject(project);
            comboActiveSprint.getItems().setAll(sprints);
            if (!sprints.isEmpty()) {
                comboActiveSprint.setValue(sprints.get(0));
            } else {
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
            loadProjects();
        }
        showDashboard();
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
                // Dashboard also shows project list, might need to sync
            } else if (controller instanceof SprintsViewController) {
                SprintsViewController svc = (SprintsViewController) controller;
                svc.setProject(comboActiveProject.getValue());
                svc.setDashboardController(null);
            } else if (controller instanceof BacklogController) {
                BacklogController bc = (BacklogController) controller;
                bc.setProject(comboActiveProject.getValue());
                if (authService != null)
                    bc.setCurrentUser(authService.getCurrentUser());
            }

            contentArea.getChildren().setAll(view);
            if (triggerButton != null)
                setActiveButton(triggerButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/view/dashboard.fxml", btnDashboard);
    }

    @FXML
    private void showProjects() {
        loadView("/view/dashboard.fxml", btnProjects);
    }

    @FXML
    private void showSprints() {
        loadView("/view/sprints-view.fxml", btnSprints);
    }

    @FXML
    private void showBacklog() {
        loadView("/view/backlog.fxml", btnBacklog);
    }

    @FXML
    private void showKanban() {
        currentViewFxml = "KANBAN";
        try {
            kanbanController controller = new kanbanController();
            if (authService != null) {
                controller.setUser(authService.getCurrentUser());
            }
            // Pass the active context
            controller.setInitialContext(comboActiveProject.getValue(), comboActiveSprint.getValue());

            Parent kanbanView = controller.createView();
            contentArea.getChildren().setAll(kanbanView);
            setActiveButton(btnKanban);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showSettings() {
        System.out.println("Show Settings");
        setActiveButton(btnSettings);
    }

    @FXML
    private void handleLogout() {
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
