package com.gestionprojet.dao;

import com.gestionprojet.model.User;
import com.gestionprojet.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDAO {

    private final Session session;

    public UserDAO() {
        this.session = HibernateUtil.getSessionFactory().openSession();
    }

    public User save(User user) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (user.getId() == null) {
                session.persist(user);
                session.flush(); // assure que l'ID est généré
            } else {
                session.merge(user);
            }

            tx.commit();
            return user;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Erreur de sauvegarde utilisateur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findById(int id) {
        User user = session.get(User.class, id);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByUsername(String username) {
        try {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username",
                    User.class
            );
            query.setParameter("username", username);

            return Optional.ofNullable(query.uniqueResult());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :email",
                    User.class
            );
            query.setParameter("email", email);

            return Optional.ofNullable(query.uniqueResult());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        Query<User> query = session.createQuery(
                "FROM User u WHERE u.isHidden = false",
                User.class
        );
        return query.list();
    }

    public List<User> findAllIncludingHidden() {
        Query<User> query = session.createQuery(
                "FROM User",
                User.class
        );
        return query.list();
    }

    public List<User> findActiveUsers() {
        Query<User> query = session.createQuery(
                "FROM User u WHERE u.isActive = true AND u.isHidden = false",
                User.class
        );
        return query.list();
    }

    public List<User> findInactiveUsers() {
        Query<User> query = session.createQuery(
                "FROM User u WHERE u.isActive = false AND u.isHidden = false",
                User.class
        );
        return query.list();
    }

    public List<User> findHiddenUsers() {
        Query<User> query = session.createQuery(
                "FROM User u WHERE u.isHidden = true",
                User.class
        );
        return query.list();
    }

    public void hideUser(User user) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            user.setHidden(true);
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur masquage utilisateur", e);
        }
    }

    public void delete(User user) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (!session.contains(user)) {
                user = session.merge(user);
            }

            session.remove(user);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur suppression utilisateur", e);
        }
    }

    public void toggleUserStatus(User user) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            user.setActive(!user.isActive());
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur changement statut utilisateur", e);
        }
    }

    public boolean existsByUsername(String username) {
        Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                Long.class
        );
        query.setParameter("username", username);
        return query.uniqueResult() > 0;
    }

    public boolean existsByEmail(String email) {
        Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                Long.class
        );
        query.setParameter("email", email);
        return query.uniqueResult() > 0;
    }
}
