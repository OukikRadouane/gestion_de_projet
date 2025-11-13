package com.gestionprojet;

import com.gestionprojet.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Chemin correct vers le FXML dans resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/project.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Gestion de Projet");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // ✅ Initialisation Hibernate
        Configuration config = new Configuration()
                .addAnnotatedClass(User.class)
                .configure();

        try (SessionFactory factory = config.buildSessionFactory()) {
            try (Session session = factory.openSession()) {

                Transaction transaction = session.beginTransaction();

                // Utilisateurs à insérer
                User[] users = {
                        new User("oukik", "qwerty"),
                        new User("radouane", "1234"),
                        new User("Ali", "azerty"),
                        new User("amin", "abcd")
                };

                String[] roles = {
                        "Product Owner",
                        "Scrum Master",
                        "Developer 1",
                        "Developer 2"
                };

                // Vérifie si l'utilisateur existe déjà avant insertion
                for (int i = 0; i < users.length; i++) {
                    User existingUser = session.createQuery(
                                    "from User where username = :uname", User.class)
                            .setParameter("uname", users[i].getUsername())
                            .uniqueResult();

                    if (existingUser == null) {
                        users[i].setRole(roles[i]);
                        session.persist(users[i]);
                    }
                }

                transaction.commit();
                System.out.println("✅ Utilisateurs insérés avec succès !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lancer l'application JavaFX
        launch();
    }
}
