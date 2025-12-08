package com.gestionprojet.controller;

import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.enums.SprintStatus;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class SprintController {

    @FXML
    private TextField sprintName;
    @FXML
    private TextArea sprintGoal;
    @FXML
    private DatePicker sprintStartDate;
    @FXML
    private DatePicker sprintEndDate;
    @FXML
    private ComboBox<SprintStatus> sprintStatus;
    @FXML
    private Label errorLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label titleLabel;

    private SprintDAO sprintDAO = new SprintDAO();
    private Sprint sprint;
    private Project project;
    private SprintsViewController sprintsViewController;

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
        if (sprint != null) {
            sprintName.setText(sprint.getName());
            sprintGoal.setText(sprint.getGoal() != null ? sprint.getGoal() : "");
            sprintStartDate.setValue(sprint.getStartDate());
            sprintEndDate.setValue(sprint.getEndDate());
            sprintStatus.setValue(sprint.getStatus());
            updateDurationLabel();
            if (titleLabel != null) {
                titleLabel.setText("Modifier le sprint");
            }
        } else {
            if (titleLabel != null) {
                titleLabel.setText("Créer un nouveau sprint");
            }
        }
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setSprintsViewController(SprintsViewController controller) {
        this.sprintsViewController = controller;
    }

    @FXML
    public void initialize() {
        // Initialiser le ComboBox avec les statuts
        sprintStatus.getItems().addAll(SprintStatus.values());
        sprintStatus.setValue(SprintStatus.PLANNED);
        
        // Configurer le StringConverter pour afficher les labels français
        sprintStatus.setConverter(new StringConverter<SprintStatus>() {
            @Override
            public String toString(SprintStatus status) {
                return status != null ? status.getLabel() : "";
            }

            @Override
            public SprintStatus fromString(String string) {
                return sprintStatus.getItems().stream()
                        .filter(status -> status.getLabel().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Écouter les changements de dates pour calculer la durée
        sprintStartDate.valueProperty().addListener((obs, oldVal, newVal) -> updateDurationLabel());
        sprintEndDate.valueProperty().addListener((obs, oldVal, newVal) -> updateDurationLabel());
    }

    private void updateDurationLabel() {
        if (sprintStartDate.getValue() != null && sprintEndDate.getValue() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    sprintStartDate.getValue(), sprintEndDate.getValue()) + 1;
            durationLabel.setText("Durée: " + days + " jour(s)");
            
            // Validation Scrum: durée recommandée entre 1 et 4 semaines
            if (days < 1) {
                durationLabel.setStyle("-fx-text-fill: #ef4444;");
            } else if (days > 28) {
                durationLabel.setStyle("-fx-text-fill: #f59e0b;");
            } else {
                durationLabel.setStyle("-fx-text-fill: #10b981;");
            }
        } else {
            durationLabel.setText("Durée: -");
            durationLabel.setStyle("-fx-text-fill: #64748b;");
        }
    }

    @FXML
    private void handleSave() {
        String name = sprintName.getText().trim();
        LocalDate startDate = sprintStartDate.getValue();
        LocalDate endDate = sprintEndDate.getValue();

        // Validations
        if (name.isEmpty()) {
            showError("Veuillez remplir le nom du sprint");
            return;
        }

        if (startDate == null || endDate == null) {
            showError("Veuillez sélectionner les dates de début et de fin");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showError("La date de fin doit être après la date de début");
            return;
        }

        if (project == null) {
            showError("Aucun projet associé");
            return;
        }

        // Validation Scrum: durée du sprint
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days < 1) {
            showError("La durée du sprint doit être d'au moins 1 jour");
            return;
        }

        // Validation: vérifier les chevauchements avec d'autres sprints
        if (sprint == null || !sprint.getId().equals(getSprintIdForValidation())) {
            if (hasOverlappingSprints(startDate, endDate)) {
                showError("Un sprint existe déjà sur cette période pour ce projet");
                return;
            }
        }

        try {
            if (sprint == null) {
                // Création
                sprint = new Sprint(name, startDate, endDate, project);
                sprint.setGoal(sprintGoal.getText().trim());
                sprint.setStatus(sprintStatus.getValue());
                sprintDAO.create(sprint);
            } else {
                // Modification
                sprint.setName(name);
                sprint.setGoal(sprintGoal.getText().trim());
                sprint.setStartDate(startDate);
                sprint.setEndDate(endDate);
                sprint.setStatus(sprintStatus.getValue());
                sprintDAO.update(sprint);
            }
            closeForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'enregistrement du sprint");
        }
    }

    private Long getSprintIdForValidation() {
        return sprint != null ? sprint.getId() : null;
    }

    private boolean hasOverlappingSprints(LocalDate startDate, LocalDate endDate) {
        List<Sprint> existingSprints = sprintDAO.getAllSprintsByProject(project);
        for (Sprint existingSprint : existingSprints) {
            if (sprint == null || !existingSprint.getId().equals(sprint.getId())) {
                // Vérifier le chevauchement
                if (!(endDate.isBefore(existingSprint.getStartDate()) || 
                      startDate.isAfter(existingSprint.getEndDate()))) {
                    return true;
                }
            }
        }
        return false;
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
        Stage stage = (Stage) sprintName.getScene().getWindow();
        stage.close();
        if (sprintsViewController != null) {
            sprintsViewController.refreshSprints();
        }
    }
}

