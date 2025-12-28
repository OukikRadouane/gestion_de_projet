package com.gestionprojet.service;

import com.gestionprojet.dao.SprintDAO;
import com.gestionprojet.dao.TaskDAO;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskStatus;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;

public class SprintService {
    private final SprintDAO sprintDAO;
    private final TaskDAO taskDAO;

    public SprintService() {
        this.sprintDAO = new SprintDAO();
        this.taskDAO = new TaskDAO();
    }

    public void autoCloseExpiredSprints(User currentUser) {
        LocalDate today = LocalDate.now();
        // Fetch expired sprints from DAO
        List<Sprint> expiredSprints = sprintDAO.getExpiredActiveSprints(today);

        for (Sprint sprint : expiredSprints) {
            System.out.println("Auto-clôture du sprint: " + sprint.getName());
            closeSprint(sprint, currentUser);
        }
    }

    public void closeSprint(Sprint sprint, User performer) {
        try {
            // 1. Move unfinished tasks to Backlog
            List<Task> sprintTasks = taskDAO.getBySprint(sprint);
            for (Task t : sprintTasks) {
                if (t.getStatus() != TaskStatus.DONE) {
                    t.setSprint(null);
                    t.setStatus(TaskStatus.BACKLOG);
                    t.addLog("Sprint clôturé - Tâche non terminée reportée au Backlog", performer);
                    taskDAO.update(t);
                }
            }

            // 2. Set sprint status to COMPLETED
            sprint.setStatus(SprintStatus.COMPLETED);
            sprintDAO.update(sprint);

            System.out.println("Sprint " + sprint.getName() + " clôturé avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la clôture du sprint " + sprint.getName());
            e.printStackTrace();
        }
    }
}
