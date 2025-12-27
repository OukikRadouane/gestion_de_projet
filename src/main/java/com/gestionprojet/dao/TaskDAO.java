package com.gestionprojet.dao;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.Tasks.*;
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
            List<Task> result = session
                    .createQuery(
                            "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs WHERE t.sprint.id = :sprintId ORDER BY t.priority DESC, t.deadline",
                            Task.class)
                    .setParameter("sprintId", sprint.getId()).getResultList();
            System.out.println(
                    "TaskDAO: getBySprint(ID=" + sprint.getId() + ") -> " + result.size() + " tâches trouvées");
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
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs WHERE t.project = :project ORDER BY t.priority DESC, t.deadline",
                    Task.class)
                    .setParameter("project", project)
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
            return session.createQuery(
                    "Select DISTINCT t From Task t LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs Where t.sprint.id = :sprintId and t.status= :status Order By t.priority DESC, t.deadline",
                    Task.class).setParameter("sprintId", sprint.getId()).setParameter("status", status).getResultList();
        } finally {
            session.close();
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
                    "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.logs WHERE t.project = :project AND t.sprint IS NULL ORDER BY t.priority DESC, t.deadline",
                    Task.class)
                    .setParameter("project", project)
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
