package com.gestionprojet.utils;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Task;
import com.gestionprojet.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // Instance unique de SessionFactory
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Crée une configuration Hibernate à partir de hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            // Si besoin, on peut ajouter les classes annotées explicitement
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Project.class);
            configuration.addAnnotatedClass(Task.class);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Erreur lors de la création de la SessionFactory : " + ex.getMessage());
        }
    }

    // Fournit une session Hibernate
    public static Session getSession() {
        return sessionFactory.openSession();
    }

    // Pour fermer la SessionFactory proprement à la fin du programme
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
