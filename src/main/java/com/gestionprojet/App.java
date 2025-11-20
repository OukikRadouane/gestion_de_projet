package com.gestionprojet;

import com.gestionprojet.model.User;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger le FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskDialog.fxml"));
        VBox root = loader.load(); // VBox car c'est la racine dans ton FXML

        // Créer la scène
        Scene scene = new Scene(root, 500, 600); // même taille que le FXML

        // Configurer le stage
        stage.setTitle("Formulaire Tâche");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        // ✅ Partie Hibernate (optionnelle si tu veux insérer des utilisateurs au lancement)
        Configuration config = new Configuration()
                .addAnnotatedClass(User.class)
                .configure();

        SessionFactory factory = config.buildSessionFactory();
        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
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
            user2.setRole("Developer 1");

            User user3 = new User();
            user3.setUsername("amin");
            user3.setPasswordHash("abcd");
            user3.setRole("Developer 2");

            session.persist(user);
            session.persist(user1);
            session.persist(user2);
            session.persist(user3);

            transaction.commit();
            System.out.println("✅ Utilisateurs insérés avec succès !");
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
            factory.close();
        }

        // ✅ Lancer l'interface JavaFX
        launch();
    }
}
