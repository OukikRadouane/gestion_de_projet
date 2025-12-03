package com.gestionprojet.utils;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Task;
import com.gestionprojet.model.User;
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

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Erreur lors de la création de la SessionFactory : " + ex.getMessage());
        }
    }

    // 👉 Getter officiel pour la SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // 👉 Ouvrir une session
    public static Session getSession() {
        return sessionFactory.openSession();
    }

    // 👉 Fermeture propre
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
