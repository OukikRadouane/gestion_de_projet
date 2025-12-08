package com.gestionprojet.dao;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Sprint;
import com.gestionprojet.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class SprintDAO {

    public void create(Sprint sprint) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            session.persist(sprint);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void update(Sprint sprint) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            session.merge(sprint);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Sprint sprint) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            Sprint s = session.find(Sprint.class, sprint.getId());
            if (s != null) session.remove(s);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Sprint getSprintById(Long id) {
        try (Session session = HibernateUtil.getSession()) {
            return session.find(Sprint.class, id);
        }
    }

    public List<Sprint> getAllSprintsByProject(Project project) {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery(
                            "FROM Sprint s WHERE s.project = :project ORDER BY s.startDate DESC", Sprint.class)
                    .setParameter("project", project)
                    .getResultList();
        }
    }

    public List<Sprint> getAllSprints() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery("FROM Sprint s ORDER BY s.startDate DESC", Sprint.class)
                    .getResultList();
        }
    }

    public Sprint getActiveSprintByProject(Project project) {
        try (Session session = HibernateUtil.getSession()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            return session.createQuery(
                            "FROM Sprint s WHERE s.project = :project AND s.startDate <= :today AND s.endDate >= :today", 
                            Sprint.class)
                    .setParameter("project", project)
                    .setParameter("today", today)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }
}

