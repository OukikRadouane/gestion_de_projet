package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import com.gestionprojet.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
    private List<Button> menuButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        menuButtons.add(btnDashboard);
        menuButtons.add(btnProjects);
        menuButtons.add(btnSprints);
        menuButtons.add(btnBacklog);
        menuButtons.add(btnKanban);
        menuButtons.add(btnSettings);
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        if (authService != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            lblUserName.setText(user.getUsername());
            lblUserRole.setText(user.getRole().toString());
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

    private void loadView(String fxmlPath, Button triggerButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Inject service/user if needed
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setAuthService(authService);
            } else if (controller instanceof SprintsViewController) {
                // SprintsViewController might need a project context,
                // but let's see how it's handled normally.
            }

            contentArea.getChildren().setAll(view);
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
        // For now projects might be part of dashboard or a separate view
        // Reusing dashboard as project management for now if no separate project.fxml
        // exists for listing
        loadView("/view/dashboard.fxml", btnProjects);
    }

    @FXML
    private void showSprints() {
        loadView("/view/sprints-view.fxml", btnSprints);
    }

    @FXML
    private void showBacklog() {
        // Backlog might be a filtered view of tasks or a new FXML
        // For now, let's show an empty state or reuse a view
        System.out.println("Show Backlog");
        setActiveButton(btnBacklog);
    }

    @FXML
    private void showKanban() {
        try {
            kanbanController controller = new kanbanController();
            if (authService != null) {
                controller.setUser(authService.getCurrentUser());
            }
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
        // Need to switch back to login scene
        // This logic is usually in the controller or a SceneManager
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
