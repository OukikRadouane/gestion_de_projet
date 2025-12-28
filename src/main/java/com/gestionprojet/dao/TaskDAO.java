package com.gestionprojet.dao;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.Tasks.*;
import com.gestionprojet.model.enums.SprintStatus;
import org.hibernate.query.Query;
import com.gestionprojet.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public Task save(Task task) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();

            // Apply backlog rules before persisting
            applyBacklogRules(task, session);

            Task savedTask = session.merge(task);
            session.getTransaction().commit();
            return savedTask;
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Task update(Task task) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();

            // Apply backlog rules before updating
            applyBacklogRules(task, session);

            Task updatedTask = session.merge(task);
            session.getTransaction().commit();
            return updatedTask;
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            Task task = session.find(Task.class, id);
            if (task != null) {
                session.remove(task);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Task getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.find(Task.class, id);
        } finally {
            session.close();
        }
    }

    public List<Task> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("SELECT t FROM Task t ORDER BY t.deadline", Task.class).list();
        } finally {
            session.close();
        }
    }

    public List<Task> getBySprint(Sprint sprint) {
        if (sprint == null || sprint.getId() == null)
            return new ArrayList<>();

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Include backlog tasks for the sprint's project so backlog is always visible
            Long projectId = null;
            try {
                Sprint managedSprint = session.find(Sprint.class, sprint.getId());
                if (managedSprint != null && managedSprint.getProject() != null)
                    projectId = managedSprint.getProject().getId();
            } catch (Exception ignored) {
            }

            StringBuilder hql = new StringBuilder();
            hql.append(
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.sprint s LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs WHERE ");
            if (projectId != null) {
                hql.append("(s.id = :sprintId OR (s.id IS NULL AND p.id = :projectId)) ");
            } else {
                hql.append("s.id = :sprintId ");
            }
            hql.append("ORDER BY t.priority DESC, t.deadline");

            Query<Task> q = session.createQuery(hql.toString(), Task.class).setParameter("sprintId", sprint.getId());
            if (projectId != null)
                q.setParameter("projectId", projectId);

            List<Task> result = q.getResultList();
            System.out.println("TaskDAO: getBySprint(ID=" + sprint.getId() + ") -> " + result.size()
                    + " tâches trouvées (inclut backlog)");
            return result;
        } finally {
            session.close();
        }
    }

    public List<Task> getByProject(Project project) {
        if (project == null || project.getId() == null)
            return new ArrayList<>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Task> result = session.createQuery(
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.sprint s LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs "
                            +
                            "WHERE p.id = :projectId OR (p.id IS NULL AND s.project.id = :projectId) " +
                            "ORDER BY t.priority DESC, t.deadline",
                    Task.class)
                    .setParameter("projectId", project.getId())
                    .getResultList();
            System.out.println(
                    "TaskDAO: getByProject(ID=" + project.getId() + ") -> " + result.size() + " tâches trouvées");
            return result;
        } finally {
            session.close();
        }
    }

    public List<Task> getBySprintAndStatus(Sprint sprint, TaskStatus status) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Long projectId = null;
            try {
                Sprint managedSprint = session.find(Sprint.class, sprint.getId());
                if (managedSprint != null && managedSprint.getProject() != null)
                    projectId = managedSprint.getProject().getId();
            } catch (Exception ignored) {
            }

            StringBuilder hql = new StringBuilder();
            hql.append(
                    "Select DISTINCT t From Task t LEFT JOIN FETCH t.sprint s LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs Where ");
            if (projectId != null) {
                hql.append(
                        "(s.id = :sprintId OR (s.id IS NULL AND p.id = :projectId)) and t.status= :status Order By t.priority DESC, t.deadline");
            } else {
                hql.append("s.id = :sprintId and t.status= :status Order By t.priority DESC, t.deadline");
            }

            Query<Task> q = session.createQuery(hql.toString(), Task.class).setParameter("sprintId", sprint.getId())
                    .setParameter("status", status);
            if (projectId != null)
                q.setParameter("projectId", projectId);
            return q.getResultList();
        } finally {
            session.close();
        }
    }

    // Apply backlog rules: if task has no sprint, or its sprint is finished but
    // task not DONE,
    // move to backlog (sprint=null) and set priority to HIGH.
    private void applyBacklogRules(Task task, Session session) {
        if (task == null)
            return;

        // If no sprint assigned -> backlog
        if (task.getSprint() == null) {
            task.setPriority(Priority.HIGH);
            return;
        }

        // If sprint assigned, fetch managed sprint to inspect status
        try {
            Sprint managedSprint = session.find(Sprint.class, task.getSprint().getId());
            if (managedSprint == null) {
                // Sprint doesn't exist -> move to backlog
                task.setSprint(null);
                task.setPriority(Priority.HIGH);
                return;
            }

            // If sprint finished and task not DONE -> move to backlog
            if (managedSprint.getStatus() == SprintStatus.COMPLETED && task.getStatus() != TaskStatus.DONE) {
                task.setSprint(null);
                task.setPriority(Priority.HIGH);
            }
        } catch (Exception e) {
            // conservative fallback: ensure backlog priority when uncertainty
            if (task.getSprint() == null)
                task.setPriority(Priority.HIGH);
        }
    }

    // Move all tasks linked to a deleted sprint to backlog and mark priority HIGH
    public void moveTasksToBacklogForDeletedSprint(Long sprintId) {
        if (sprintId == null)
            return;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                session.beginTransaction();
                List<Task> tasks = session.createQuery("SELECT t FROM Task t WHERE t.sprint.id = :sprintId", Task.class)
                        .setParameter("sprintId", sprintId).getResultList();
                for (Task t : tasks) {
                    t.setSprint(null);
                    t.setPriority(Priority.HIGH);
                    session.merge(t);
                }
                session.getTransaction().commit();
            } catch (Exception e) {
                if (session.getTransaction().isActive())
                    session.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<Task> getByProjectAndSprint(Project project, Sprint sprint) {
        if (project == null || project.getId() == null || sprint == null || sprint.getId() == null)
            return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs WHERE t.project = :project AND t.sprint.id = :sprintId ORDER BY t.priority DESC, t.deadline",
                    Task.class)
                    .setParameter("project", project)
                    .setParameter("sprintId", sprint.getId())
                    .getResultList();
        }
    }

    public List<Task> getBacklogByProject(Project project) {
        if (project == null || project.getId() == null)
            return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.sprint s LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs "
                            +
                            "WHERE (p.id = :projectId OR (p.id IS NULL AND s.project.id = :projectId)) " +
                            "AND s.id IS NULL " +
                            "ORDER BY t.priority DESC, t.deadline",
                    Task.class)
                    .setParameter("projectId", project.getId())
                    .getResultList();
        }
    }

    public Task getByIdWithCollections(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Charger la tâche
            Task task = session.find(Task.class, id);

            // Initialiser explicitement les collections
            if (task != null) {
                Hibernate.initialize(task.getComments());
                Hibernate.initialize(task.getSubtasks());
                Hibernate.initialize(task.getLogs());
                Hibernate.initialize(task.getProject());
                Hibernate.initialize(task.getSprint());
                if (task.getProject() != null) {
                    Hibernate.initialize(task.getProject().getName());
                }
                task.setComments(new java.util.ArrayList<>(task.getComments()));
                task.setSubtasks(new java.util.ArrayList<>(task.getSubtasks()));
                task.setLogs(new java.util.ArrayList<>(task.getLogs()));
            }

            return task;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour supprimer une sous-tâche
    public void deleteSubtask(Subtask subtask) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();

            // Recharger la sous-tâche depuis la base de données pour s'assurer qu'elle est
            // gérée
            Subtask managedSubtask = session.find(Subtask.class, subtask.getId());
            if (managedSubtask != null) {
                session.remove(managedSubtask);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    // Méthode pour supprimer un commentaire
    public void deleteComment(Comment comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();

            // Recharger le commentaire depuis la base de données pour s'assurer qu'il est
            // géré
            Comment managedComment = session.find(Comment.class, comment.getId());
            if (managedComment != null) {
                session.remove(managedComment);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

}
