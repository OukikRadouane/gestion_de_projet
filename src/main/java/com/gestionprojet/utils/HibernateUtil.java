package com.gestionprojet.utils;

import com.gestionprojet.model.Tasks.Comment;
import com.gestionprojet.model.Tasks.Subtask;
import com.gestionprojet.model.Tasks.Task;
import com.gestionprojet.model.Tasks.TaskLog;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure();

                configuration.addAnnotatedClass(Task.class);
                configuration.addAnnotatedClass(Comment.class);
                configuration.addAnnotatedClass(Subtask.class);
                configuration.addAnnotatedClass(TaskLog.class);

                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la création de la SessionFactory : " + e.getMessage());
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
