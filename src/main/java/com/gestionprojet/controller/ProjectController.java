package com.gestionprojet.controller;

import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    Project project = new Project();
    @FXML
    private void handleSave(){
        String name = ProjectName.getText().trim();
        String desc = ProjectDesc.getText().trim();

        if(name.isEmpty()){
            showError("Veuillez remplir le champ de Nom ");
            return;
        }
        try{
            if( project == null ){
                project = new Project(name,desc, SessionManager.getInstance().getCurrentUser());
                if(ProjectStartDate.getValue() != null || ProjectEndDate.getValue() != null){
                    project.setStartDate(ProjectStartDate.getValue());
                    project.setEndDate(ProjectEndDate.getValue());
                }
                projectDAO.create(project);
            }
            else{
                project.setName(name);
                project.setDescription(desc);
                project.setStartDate(ProjectStartDate.getValue());
                project.setEndDate(ProjectEndDate.getValue());
                project.setCreator(SessionManager.getInstance().getCurrentUser());
                projectDAO.update(project);
            }
            resetForm();
        }
        catch (Exception ex){
            ex.printStackTrace();
            showError("Erreur dans l'enregistrement de projet");
        }
    }
    @FXML
    private void handleCancel(){ closeForm();}
    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    private void closeForm(){
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
}
