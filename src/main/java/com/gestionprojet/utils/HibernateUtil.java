package com.gestionprojet.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // Instance unique de SessionFactory
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Chargement du fichier de configuration hibernate.cfg.xml
                sessionFactory = new Configuration().configure().buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la création de la SessionFactory : " + e.getMessage());
            }
        }
        return sessionFactory; // ✅ ici on retourne bien quelque chose
    }

    // Pour fermer la SessionFactory proprement à la fin du programme
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
