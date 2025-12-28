package com.gestionprojet.controller;

import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class ProjectController {

    @FXML
    private TextField ProjectName;
    @FXML
    private TextArea ProjectDesc;
    @FXML
    private DatePicker ProjectStartDate;
    @FXML
    private DatePicker ProjectEndDate;

    @FXML
    private Label errorLabel;

    ProjectDAO projectDAO = new ProjectDAO();

    private Project project;
    private DashboardController dashboardController;

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            ProjectName.setText(project.getName());
            ProjectDesc.setText(project.getDescription());
            ProjectStartDate.setValue(project.getStartDate());
            ProjectEndDate.setValue(project.getEndDate());
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void handleSave() {
        String name = ProjectName.getText().trim();
        String desc = ProjectDesc.getText().trim();

        if (name.isEmpty()) {
            showError("Veuillez remplir le champ de Nom ");
            return;
        }

        Role currentRole = SessionManager.getInstance().getCurrentUser().getRole();
        if (currentRole != Role.ADMIN && currentRole != Role.PRODUCT_OWNER) {
            showError("Vous n'avez pas les droits pour " + (project == null ? "créer" : "modifier") + " un projet.");
            return;
        }

        try {
            if (project == null) {
                project = new Project(name, desc, SessionManager.getInstance().getCurrentUser());
                if (ProjectStartDate.getValue() != null || ProjectEndDate.getValue() != null) {
                    project.setStartDate(ProjectStartDate.getValue());
                    project.setEndDate(ProjectEndDate.getValue());
                }
                projectDAO.create(project);
            } else {
                project.setName(name);
                project.setDescription(desc);
                project.setStartDate(ProjectStartDate.getValue());
                project.setEndDate(ProjectEndDate.getValue());
                project.setCreator(SessionManager.getInstance().getCurrentUser());
                projectDAO.update(project);
            }
            refreshDashboardProjects();
            closeForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur dans l'enregistrement de projet");
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeForm() {
        Stage stage = (Stage) ProjectName.getScene().getWindow();
        stage.close();
    }

    private void resetForm() {
        ProjectName.clear();
        ProjectDesc.clear();
        ProjectStartDate.setValue(null);
        ProjectEndDate.setValue(null);
        errorLabel.setVisible(false);
        project = null;
    }

    private void refreshDashboardProjects() {
        if (dashboardController == null) {
            return;
        }
        try {
            List<Project> projects = projectDAO.getAllProjectsByUser(SessionManager.getInstance().getCurrentUser());
            dashboardController.refreshProjects(projects);
        } catch (Exception e) {
            e.printStackTrace();
            // On ne bloque pas l'utilisateur; l'erreur sera visible dans la console
        }
    }
}
