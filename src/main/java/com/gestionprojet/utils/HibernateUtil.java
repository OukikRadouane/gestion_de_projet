package com.gestionprojet.utils;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import com.gestionprojet.model.Tasks.Comment;
import com.gestionprojet.model.Tasks.Subtask;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskLog;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Project.class);
            configuration.addAnnotatedClass(Task.class);
            configuration.addAnnotatedClass(Comment.class);
            configuration.addAnnotatedClass(Subtask.class);
            configuration.addAnnotatedClass(TaskLog.class);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(
                    "Erreur lors de la création de la SessionFactory : " + ex.getMessage()
            );
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
