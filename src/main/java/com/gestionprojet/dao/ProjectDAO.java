package com.gestionprojet.dao;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import com.gestionprojet.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ProjectDAO {

    public void create(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            session.persist(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void update(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            session.merge(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSession()) {
            tx = session.beginTransaction();
            Project p = session.find(Project.class, project.getId());
            if (p != null) session.remove(p);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Project getProjectById(Long id) {
        try (Session session = HibernateUtil.getSession()) {
            return session.find(Project.class, id);
        }
    }

    public List<Project> getAllProjectsByUser(User creator) {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery(
                            "FROM Project p WHERE p.creator = :creator ORDER BY p.createdAt DESC", Project.class)
                    .setParameter("creator", creator)
                    .getResultList();
        }
    }

    public List<Project> getAllProjects() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery("FROM Project p ORDER BY p.createdAt DESC", Project.class)
                    .getResultList();
        }
    }
}
