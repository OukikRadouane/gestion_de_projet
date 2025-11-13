package com.gestionprojet;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch();
        /*

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
         */

    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/signup.fxml")));
        stage.setTitle("Login");
        stage.setScene(new Scene(root,600,600));
        stage.show();
    }
}