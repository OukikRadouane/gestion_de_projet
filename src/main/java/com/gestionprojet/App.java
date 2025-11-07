package com.gestionprojet;
import com.gestionprojet.model.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class App {
    public static void main(String[] args) {
        User user = new User();
        user.setUsername("oukik");
        user.setPasswordHash("qwerty");
        user.setRole("Product Owner");
        User user1 = new User();
        user1.setUsername("radouane");
        user1.setPasswordHash("1234");
        user1.setRole("Scrum Master");
        User user2 = new User();
        user2.setUsername("Ali");
        user2.setPasswordHash("azerty");
        user2.setRole("Developper 1");
        User user3 = new User();
        user3.setUsername("amin");
        user3.setPasswordHash("abcd");
        user3.setRole("Developper 2");

        Configuration config = new Configuration()
                .addAnnotatedClass(User.class)
                .configure();
        SessionFactory factory = config.buildSessionFactory();
        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(user);
        session.persist(user1);
        session.persist(user2);
        session.persist(user3);
        transaction.commit();
    }
}